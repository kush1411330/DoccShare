package com.doccshare.analytics;

import com.doccshare.analytics.dto.*;
import com.doccshare.common.exception.ResourceNotFoundException;
import com.doccshare.file.FileRepository;
import com.doccshare.file.FileStatus;
import com.doccshare.file.StoredFile;
import com.doccshare.share.ShareLink;
import com.doccshare.share.ShareLinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final DownloadEventRepository downloadEventRepository;
    private final FileRepository fileRepository;
    private final ShareLinkRepository shareLinkRepository;
    private final UserAgentParser userAgentParser;
    private final GeoIpService geoIpService;

    @Value("${DoccShare.app.ip-hash-salt:DoccShare-salt-for-privacy-preserving-hashing}")
    private String ipHashSalt;

    public AnalyticsService(
            DownloadEventRepository downloadEventRepository,
            FileRepository fileRepository,
            ShareLinkRepository shareLinkRepository,
            UserAgentParser userAgentParser,
            GeoIpService geoIpService
    ) {
        this.downloadEventRepository = downloadEventRepository;
        this.fileRepository = fileRepository;
        this.shareLinkRepository = shareLinkRepository;
        this.userAgentParser = userAgentParser;
        this.geoIpService = geoIpService;
    }

    @Async("analyticsExecutor")
    @Transactional
    public void recordDownload(UUID shareLinkId, UUID fileId, String clientIp, String userAgent, String referrer) {
        try {
            ShareLink shareLink = shareLinkRepository.findById(shareLinkId).orElse(null);
            StoredFile file = fileRepository.findById(fileId).orElse(null);

            if (shareLink == null || file == null) {
                log.warn("Cannot record download event: missing shareLink ({}) or file ({})", shareLinkId, fileId);
                return;
            }

            String ipHash = hashIp(clientIp);
            UserAgentParser.UserAgentInfo uaInfo = userAgentParser.parse(userAgent);
            GeoIpService.GeoLocation geo = geoIpService.resolve(clientIp);

            DownloadEvent event = DownloadEvent.builder()
                    .shareLink(shareLink)
                    .file(file)
                    .ipHash(ipHash)
                    .countryCode(geo.countryCode())
                    .countryName(geo.countryName())
                    .city(geo.city())
                    .browser(uaInfo.browser())
                    .os(uaInfo.os())
                    .deviceType(uaInfo.deviceType())
                    .referrer(referrer != null && referrer.length() > 512 ? referrer.substring(0, 512) : referrer)
                    .build();

            downloadEventRepository.save(event);
            log.debug("Recorded download event for file: {}, link: {}, country: {}, browser: {}",
                    file.getId(), shareLink.getId(), geo.countryName(), uaInfo.browser());
        } catch (Exception e) {
            log.error("Failed to asynchronously record download event: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public AnalyticsOverviewDto getOverview(UUID userId) {
        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);

        long totalFiles = fileRepository.countByOwnerIdAndStatus(userId, FileStatus.READY);
        long totalLinks = shareLinkRepository.countByFileOwnerId(userId);
        long totalDownloads = shareLinkRepository.sumDownloadsByFileOwnerId(userId);
        long uniqueVisitors30Days = downloadEventRepository.countUniqueVisitorsByOwnerSince(userId, thirtyDaysAgo);
        long downloads30Days = downloadEventRepository.countDownloadsByOwnerSince(userId, thirtyDaysAgo);

        return AnalyticsOverviewDto.builder()
                .totalFiles(totalFiles)
                .totalLinks(totalLinks)
                .totalDownloads(totalDownloads)
                .uniqueVisitors30Days(uniqueVisitors30Days)
                .downloads30Days(downloads30Days)
                .build();
    }

    @Transactional(readOnly = true)
    public FileAnalyticsDto getFileAnalytics(UUID fileId, UUID userId, Instant from, Instant to) {
        fileRepository.findByIdAndOwnerId(fileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        Instant effectiveFrom = from != null ? from : Instant.now().minus(30, ChronoUnit.DAYS);
        Instant effectiveTo = to != null ? to : Instant.now();

        long totalDownloads = downloadEventRepository.countDownloadsByFile(fileId, effectiveFrom, effectiveTo);
        long uniqueVisitors = downloadEventRepository.countUniqueVisitorsByFile(fileId, effectiveFrom, effectiveTo);

        List<TimeSeriesDto> timeSeries = downloadEventRepository.aggregateFileTimeSeries(fileId, effectiveFrom, effectiveTo)
                .stream()
                .map(p -> new TimeSeriesDto(p.getDate(), p.getCount()))
                .toList();

        List<GeoBreakdownDto> countries = downloadEventRepository.aggregateFileCountries(fileId, effectiveFrom, effectiveTo)
                .stream()
                .map(p -> new GeoBreakdownDto(p.getCountryCode(), p.getCountryName(), p.getCount()))
                .toList();

        List<ItemBreakdownDto> browsers = downloadEventRepository.aggregateFileBrowsers(fileId, effectiveFrom, effectiveTo)
                .stream()
                .map(p -> new ItemBreakdownDto(p.getItem(), p.getCount()))
                .toList();

        List<ItemBreakdownDto> operatingSystems = downloadEventRepository.aggregateFileOperatingSystems(fileId, effectiveFrom, effectiveTo)
                .stream()
                .map(p -> new ItemBreakdownDto(p.getItem(), p.getCount()))
                .toList();

        List<ItemBreakdownDto> devices = downloadEventRepository.aggregateFileDevices(fileId, effectiveFrom, effectiveTo)
                .stream()
                .map(p -> new ItemBreakdownDto(p.getItem(), p.getCount()))
                .toList();

        List<RecentDownloadDto> recent = downloadEventRepository.findRecentByFile(fileId, PageRequest.of(0, 30))
                .stream()
                .map(RecentDownloadDto::fromEntity)
                .toList();

        return FileAnalyticsDto.builder()
                .totalDownloads(totalDownloads)
                .uniqueVisitors(uniqueVisitors)
                .timeSeries(timeSeries)
                .countries(countries)
                .browsers(browsers)
                .operatingSystems(operatingSystems)
                .devices(devices)
                .recent(recent)
                .build();
    }

    @Transactional(readOnly = true)
    public FileAnalyticsDto getShareLinkAnalytics(UUID shareLinkId, UUID userId, Instant from, Instant to) {
        shareLinkRepository.findByIdAndFileOwnerId(shareLinkId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Share link not found"));

        Instant effectiveFrom = from != null ? from : Instant.now().minus(30, ChronoUnit.DAYS);
        Instant effectiveTo = to != null ? to : Instant.now();

        long totalDownloads = downloadEventRepository.countDownloadsByShareLink(shareLinkId, effectiveFrom, effectiveTo);
        long uniqueVisitors = downloadEventRepository.countUniqueVisitorsByShareLink(shareLinkId, effectiveFrom, effectiveTo);

        List<TimeSeriesDto> timeSeries = downloadEventRepository.aggregateShareLinkTimeSeries(shareLinkId, effectiveFrom, effectiveTo)
                .stream()
                .map(p -> new TimeSeriesDto(p.getDate(), p.getCount()))
                .toList();

        List<GeoBreakdownDto> countries = downloadEventRepository.aggregateShareLinkCountries(shareLinkId, effectiveFrom, effectiveTo)
                .stream()
                .map(p -> new GeoBreakdownDto(p.getCountryCode(), p.getCountryName(), p.getCount()))
                .toList();

        List<ItemBreakdownDto> browsers = downloadEventRepository.aggregateShareLinkBrowsers(shareLinkId, effectiveFrom, effectiveTo)
                .stream()
                .map(p -> new ItemBreakdownDto(p.getItem(), p.getCount()))
                .toList();

        List<ItemBreakdownDto> operatingSystems = downloadEventRepository.aggregateShareLinkOperatingSystems(shareLinkId, effectiveFrom, effectiveTo)
                .stream()
                .map(p -> new ItemBreakdownDto(p.getItem(), p.getCount()))
                .toList();

        List<ItemBreakdownDto> devices = downloadEventRepository.aggregateShareLinkDevices(shareLinkId, effectiveFrom, effectiveTo)
                .stream()
                .map(p -> new ItemBreakdownDto(p.getItem(), p.getCount()))
                .toList();

        List<RecentDownloadDto> recent = downloadEventRepository.findRecentByShareLink(shareLinkId, PageRequest.of(0, 30))
                .stream()
                .map(RecentDownloadDto::fromEntity)
                .toList();

        return FileAnalyticsDto.builder()
                .totalDownloads(totalDownloads)
                .uniqueVisitors(uniqueVisitors)
                .timeSeries(timeSeries)
                .countries(countries)
                .browsers(browsers)
                .operatingSystems(operatingSystems)
                .devices(devices)
                .recent(recent)
                .build();
    }

    private String hashIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(ipHashSalt.getBytes(StandardCharsets.UTF_8));
            byte[] hash = digest.digest(ip.trim().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            log.error("Failed to hash IP: {}", e.getMessage());
            return null;
        }
    }
}

