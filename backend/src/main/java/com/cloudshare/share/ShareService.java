package com.cloudshare.share;

import com.cloudshare.analytics.AnalyticsService;
import com.cloudshare.common.exception.BadRequestException;
import com.cloudshare.common.exception.ResourceNotFoundException;
import com.cloudshare.common.exception.UnauthorizedException;
import com.cloudshare.file.FileRepository;
import com.cloudshare.file.StoredFile;
import com.cloudshare.share.dto.*;
import com.cloudshare.storage.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class ShareService {

    private static final Logger log = LoggerFactory.getLogger(ShareService.class);

    private final ShareLinkRepository shareLinkRepository;
    private final FileRepository fileRepository;
    private final StorageService storageService;
    private final AnalyticsService analyticsService;
    private final PasswordEncoder passwordEncoder;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${cloudshare.app.public-base-url:http://localhost:4200}")
    private String publicBaseUrl;

    @Value("${cloudshare.storage.download-url-expiry-seconds:60}")
    private long downloadUrlExpirySeconds;

    public ShareService(
            ShareLinkRepository shareLinkRepository,
            FileRepository fileRepository,
            StorageService storageService,
            AnalyticsService analyticsService,
            PasswordEncoder passwordEncoder
    ) {
        this.shareLinkRepository = shareLinkRepository;
        this.fileRepository = fileRepository;
        this.storageService = storageService;
        this.analyticsService = analyticsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public ShareResponse createShareLink(UUID fileId, UUID userId, CreateShareRequest request) {
        StoredFile file = fileRepository.findByIdAndOwnerId(fileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        String token = generateSecureToken();
        String passwordHash = request.getPassword() != null && !request.getPassword().isBlank()
                ? passwordEncoder.encode(request.getPassword())
                : null;

        ShareLink shareLink = ShareLink.builder()
                .file(file)
                .token(token)
                .passwordHash(passwordHash)
                .expiresAt(request.getExpiresAt())
                .maxDownloads(request.getMaxDownloads())
                .downloadCount(0)
                .active(true)
                .build();

        shareLink = shareLinkRepository.save(shareLink);
        log.info("Created share link with token: {} for file: {}", token, fileId);

        return ShareResponse.fromEntity(shareLink, publicBaseUrl);
    }

    @Transactional(readOnly = true)
    public List<ShareResponse> listShareLinks(UUID fileId, UUID userId) {
        fileRepository.findByIdAndOwnerId(fileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        return shareLinkRepository.findByFileIdAndFileOwnerId(fileId, userId)
                .stream()
                .map(link -> ShareResponse.fromEntity(link, publicBaseUrl))
                .toList();
    }

    @Transactional
    public ShareResponse updateShareLink(UUID shareLinkId, UUID userId, UpdateShareRequest request) {
        ShareLink link = shareLinkRepository.findByIdAndFileOwnerId(shareLinkId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Share link not found"));

        if (request.getActive() != null) {
            link.setActive(request.getActive());
        }

        if (Boolean.TRUE.equals(request.getClearPassword())) {
            link.setPasswordHash(null);
        } else if (request.getPassword() != null && !request.getPassword().isBlank()) {
            link.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (Boolean.TRUE.equals(request.getClearExpiry())) {
            link.setExpiresAt(null);
        } else if (request.getExpiresAt() != null) {
            link.setExpiresAt(request.getExpiresAt());
        }

        if (Boolean.TRUE.equals(request.getClearMaxDownloads())) {
            link.setMaxDownloads(null);
        } else if (request.getMaxDownloads() != null) {
            link.setMaxDownloads(request.getMaxDownloads());
        }

        link = shareLinkRepository.save(link);
        return ShareResponse.fromEntity(link, publicBaseUrl);
    }

    @Transactional
    public void revokeShareLink(UUID shareLinkId, UUID userId) {
        ShareLink link = shareLinkRepository.findByIdAndFileOwnerId(shareLinkId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Share link not found"));

        shareLinkRepository.delete(link);
        log.info("Revoked and deleted share link: {}", shareLinkId);
    }

    @Transactional(readOnly = true)
    public PublicShareMetadata getPublicShareMetadata(String token) {
        ShareLink link = shareLinkRepository.findByToken(token)
                .filter(ShareLink::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Share link not found or inactive"));

        boolean isExpired = link.getExpiresAt() != null && link.getExpiresAt().isBefore(Instant.now());
        boolean isMaxReached = link.getMaxDownloads() != null && link.getDownloadCount() >= link.getMaxDownloads();

        return PublicShareMetadata.builder()
                .fileName(link.getFile().getOriginalName())
                .sizeBytes(link.getFile().getSizeBytes())
                .contentType(link.getFile().getContentType())
                .passwordRequired(link.getPasswordHash() != null)
                .expired(isExpired)
                .maxDownloadsReached(isMaxReached)
                .expiresAt(link.getExpiresAt())
                .build();
    }

    @Transactional
    public DownloadResponse processDownload(String token, DownloadRequest request, HttpServletRequest httpRequest) {
        ShareLink link = shareLinkRepository.findByToken(token)
                .filter(ShareLink::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Share link not found or inactive"));

        Instant now = Instant.now();
        if (link.getExpiresAt() != null && link.getExpiresAt().isBefore(now)) {
            throw new BadRequestException("This share link has expired");
        }

        if (link.getMaxDownloads() != null && link.getDownloadCount() >= link.getMaxDownloads()) {
            throw new BadRequestException("Maximum download limit reached for this share link");
        }

        if (link.getPasswordHash() != null) {
            if (request == null || request.getPassword() == null || request.getPassword().isBlank()) {
                throw new UnauthorizedException("Password is required to download this file");
            }
            if (!passwordEncoder.matches(request.getPassword(), link.getPasswordHash())) {
                throw new UnauthorizedException("Incorrect password");
            }
        }

        int updatedRows = shareLinkRepository.incrementDownloadCountSafely(link.getId(), now);
        if (updatedRows == 0) {
            throw new BadRequestException("Download could not be completed (link expired or download limit reached)");
        }

        String clientIp = extractClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String referrer = httpRequest.getHeader("Referer");
        analyticsService.recordDownload(link.getId(), link.getFile().getId(), clientIp, userAgent, referrer);

        String downloadUrl = storageService.createDownloadUrl(
                link.getFile().getStorageKey(),
                link.getFile().getOriginalName(),
                Duration.ofSeconds(downloadUrlExpirySeconds)
        );

        return DownloadResponse.builder()
                .downloadUrl(downloadUrl)
                .expiresInSeconds(downloadUrlExpirySeconds)
                .build();
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[24];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
