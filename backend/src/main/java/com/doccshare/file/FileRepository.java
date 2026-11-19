package com.doccshare.file;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<StoredFile, UUID> {

    Optional<StoredFile> findByIdAndOwnerId(UUID id, UUID ownerId);

    Page<StoredFile> findByOwnerIdAndStatus(UUID ownerId, FileStatus status, Pageable pageable);

    @Query("SELECT f FROM StoredFile f WHERE f.owner.id = :ownerId AND f.status = :status AND LOWER(f.originalName) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<StoredFile> searchByOwnerIdAndStatus(@Param("ownerId") UUID ownerId, @Param("status") FileStatus status, @Param("query") String query, Pageable pageable);

    long countByOwnerIdAndStatus(UUID ownerId, FileStatus status);

    List<StoredFile> findByStatusAndCreatedAtBefore(FileStatus status, Instant cutoff);
}

