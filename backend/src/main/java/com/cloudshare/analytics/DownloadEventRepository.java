package com.cloudshare.analytics;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface DownloadEventRepository extends JpaRepository<DownloadEvent, UUID> {

    // Projection interfaces for SQL aggregations
    interface TimeSeriesProjection {
        String getDate();
        Long getCount();
    }

    interface CountryProjection {
        String getCountryCode();
        String getCountryName();
        Long getCount();
    }

    interface ItemCountProjection {
        String getItem();
        Long getCount();
    }

    // --- Overview for all user files ---
    @Query("SELECT COUNT(DISTINCT d.ipHash) FROM DownloadEvent d WHERE d.file.owner.id = :ownerId AND d.downloadedAt >= :since")
    long countUniqueVisitorsByOwnerSince(@Param("ownerId") UUID ownerId, @Param("since") Instant since);

    @Query("SELECT COUNT(d) FROM DownloadEvent d WHERE d.file.owner.id = :ownerId AND d.downloadedAt >= :since")
    long countDownloadsByOwnerSince(@Param("ownerId") UUID ownerId, @Param("since") Instant since);

    // --- File-level analytics ---
    @Query("SELECT COUNT(d) FROM DownloadEvent d WHERE d.file.id = :fileId AND d.downloadedAt BETWEEN :from AND :to")
    long countDownloadsByFile(@Param("fileId") UUID fileId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT COUNT(DISTINCT d.ipHash) FROM DownloadEvent d WHERE d.file.id = :fileId AND d.downloadedAt BETWEEN :from AND :to")
    long countUniqueVisitorsByFile(@Param("fileId") UUID fileId, @Param("from") Instant from, @Param("to") Instant to);

    @Query(value = "SELECT TO_CHAR(d.downloaded_at, 'YYYY-MM-DD') AS date, COUNT(*) AS count " +
                   "FROM download_events d " +
                   "WHERE d.file_id = :fileId AND d.downloaded_at BETWEEN :from AND :to " +
                   "GROUP BY TO_CHAR(d.downloaded_at, 'YYYY-MM-DD') ORDER BY date ASC", nativeQuery = true)
    List<TimeSeriesProjection> aggregateFileTimeSeries(@Param("fileId") UUID fileId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT d.countryCode AS countryCode, d.countryName AS countryName, COUNT(d) AS count " +
           "FROM DownloadEvent d WHERE d.file.id = :fileId AND d.downloadedAt BETWEEN :from AND :to " +
           "GROUP BY d.countryCode, d.countryName ORDER BY COUNT(d) DESC")
    List<CountryProjection> aggregateFileCountries(@Param("fileId") UUID fileId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT COALESCE(d.browser, 'Unknown') AS item, COUNT(d) AS count " +
           "FROM DownloadEvent d WHERE d.file.id = :fileId AND d.downloadedAt BETWEEN :from AND :to " +
           "GROUP BY d.browser ORDER BY COUNT(d) DESC")
    List<ItemCountProjection> aggregateFileBrowsers(@Param("fileId") UUID fileId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT COALESCE(d.os, 'Unknown') AS item, COUNT(d) AS count " +
           "FROM DownloadEvent d WHERE d.file.id = :fileId AND d.downloadedAt BETWEEN :from AND :to " +
           "GROUP BY d.os ORDER BY COUNT(d) DESC")
    List<ItemCountProjection> aggregateFileOperatingSystems(@Param("fileId") UUID fileId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT COALESCE(d.deviceType, 'Unknown') AS item, COUNT(d) AS count " +
           "FROM DownloadEvent d WHERE d.file.id = :fileId AND d.downloadedAt BETWEEN :from AND :to " +
           "GROUP BY d.deviceType ORDER BY COUNT(d) DESC")
    List<ItemCountProjection> aggregateFileDevices(@Param("fileId") UUID fileId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT d FROM DownloadEvent d WHERE d.file.id = :fileId ORDER BY d.downloadedAt DESC")
    List<DownloadEvent> findRecentByFile(@Param("fileId") UUID fileId, Pageable pageable);

    // --- ShareLink-level analytics ---
    @Query("SELECT COUNT(d) FROM DownloadEvent d WHERE d.shareLink.id = :shareLinkId AND d.downloadedAt BETWEEN :from AND :to")
    long countDownloadsByShareLink(@Param("shareLinkId") UUID shareLinkId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT COUNT(DISTINCT d.ipHash) FROM DownloadEvent d WHERE d.shareLink.id = :shareLinkId AND d.downloadedAt BETWEEN :from AND :to")
    long countUniqueVisitorsByShareLink(@Param("shareLinkId") UUID shareLinkId, @Param("from") Instant from, @Param("to") Instant to);

    @Query(value = "SELECT TO_CHAR(d.downloaded_at, 'YYYY-MM-DD') AS date, COUNT(*) AS count " +
                   "FROM download_events d " +
                   "WHERE d.share_link_id = :shareLinkId AND d.downloaded_at BETWEEN :from AND :to " +
                   "GROUP BY TO_CHAR(d.downloaded_at, 'YYYY-MM-DD') ORDER BY date ASC", nativeQuery = true)
    List<TimeSeriesProjection> aggregateShareLinkTimeSeries(@Param("shareLinkId") UUID shareLinkId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT d.countryCode AS countryCode, d.countryName AS countryName, COUNT(d) AS count " +
           "FROM DownloadEvent d WHERE d.shareLink.id = :shareLinkId AND d.downloadedAt BETWEEN :from AND :to " +
           "GROUP BY d.countryCode, d.countryName ORDER BY COUNT(d) DESC")
    List<CountryProjection> aggregateShareLinkCountries(@Param("shareLinkId") UUID shareLinkId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT COALESCE(d.browser, 'Unknown') AS item, COUNT(d) AS count " +
           "FROM DownloadEvent d WHERE d.shareLink.id = :shareLinkId AND d.downloadedAt BETWEEN :from AND :to " +
           "GROUP BY d.browser ORDER BY COUNT(d) DESC")
    List<ItemCountProjection> aggregateShareLinkBrowsers(@Param("shareLinkId") UUID shareLinkId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT COALESCE(d.os, 'Unknown') AS item, COUNT(d) AS count " +
           "FROM DownloadEvent d WHERE d.shareLink.id = :shareLinkId AND d.downloadedAt BETWEEN :from AND :to " +
           "GROUP BY d.os ORDER BY COUNT(d) DESC")
    List<ItemCountProjection> aggregateShareLinkOperatingSystems(@Param("shareLinkId") UUID shareLinkId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT COALESCE(d.deviceType, 'Unknown') AS item, COUNT(d) AS count " +
           "FROM DownloadEvent d WHERE d.shareLink.id = :shareLinkId AND d.downloadedAt BETWEEN :from AND :to " +
           "GROUP BY d.deviceType ORDER BY COUNT(d) DESC")
    List<ItemCountProjection> aggregateShareLinkDevices(@Param("shareLinkId") UUID shareLinkId, @Param("from") Instant from, @Param("to") Instant to);

    @Query("SELECT d FROM DownloadEvent d WHERE d.shareLink.id = :shareLinkId ORDER BY d.downloadedAt DESC")
    List<DownloadEvent> findRecentByShareLink(@Param("shareLinkId") UUID shareLinkId, Pageable pageable);
}
