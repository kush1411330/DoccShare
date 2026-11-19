package com.cloudshare.common.job;

import com.cloudshare.file.FileRepository;
import com.cloudshare.file.FileStatus;
import com.cloudshare.file.StoredFile;
import com.cloudshare.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class FileCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(FileCleanupJob.class);

    private final FileRepository fileRepository;
    private final StorageService storageService;

    public FileCleanupJob(FileRepository fileRepository, StorageService storageService) {
        this.fileRepository = fileRepository;
        this.storageService = storageService;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupAbandonedPendingFiles() {
        Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
        log.info("Running scheduled cleanup job for PENDING files created before {}", cutoff);

        List<StoredFile> abandonedFiles = fileRepository.findByStatusAndCreatedAtBefore(FileStatus.PENDING, cutoff);
        for (StoredFile file : abandonedFiles) {
            try {
                storageService.deleteObject(file.getStorageKey());
                fileRepository.delete(file);
                log.info("Cleaned up abandoned pending file: {} (id: {})", file.getOriginalName(), file.getId());
            } catch (Exception e) {
                log.warn("Failed to cleanup file {}: {}", file.getId(), e.getMessage());
            }
        }
    }
}
