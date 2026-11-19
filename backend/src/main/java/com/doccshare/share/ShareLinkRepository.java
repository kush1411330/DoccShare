package com.doccshare.share;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShareLinkRepository extends JpaRepository<ShareLink, UUID> {

    Optional<ShareLink> findByToken(String token);

    @Query("SELECT s FROM ShareLink s WHERE s.id = :id AND s.file.owner.id = :ownerId")
    Optional<ShareLink> findByIdAndFileOwnerId(@Param("id") UUID id, @Param("ownerId") UUID ownerId);

    @Query("SELECT s FROM ShareLink s WHERE s.file.id = :fileId AND s.file.owner.id = :ownerId ORDER BY s.createdAt DESC")
    List<ShareLink> findByFileIdAndFileOwnerId(@Param("fileId") UUID fileId, @Param("ownerId") UUID ownerId);

    @Modifying
    @Query("UPDATE ShareLink s SET s.downloadCount = s.downloadCount + 1 " +
           "WHERE s.id = :id AND s.active = true " +
           "AND (s.expiresAt IS NULL OR s.expiresAt > :now) " +
           "AND (s.maxDownloads IS NULL OR s.downloadCount < s.maxDownloads)")
    int incrementDownloadCountSafely(@Param("id") UUID id, @Param("now") Instant now);

    @Query("SELECT COUNT(s) FROM ShareLink s WHERE s.file.owner.id = :ownerId")
    long countByFileOwnerId(@Param("ownerId") UUID ownerId);

    @Query("SELECT COALESCE(SUM(s.downloadCount), 0) FROM ShareLink s WHERE s.file.owner.id = :ownerId")
    long sumDownloadsByFileOwnerId(@Param("ownerId") UUID ownerId);

    @Query("SELECT COALESCE(SUM(s.downloadCount), 0) FROM ShareLink s WHERE s.file.id = :fileId")
    long sumDownloadsByFileId(@Param("fileId") UUID fileId);
}

