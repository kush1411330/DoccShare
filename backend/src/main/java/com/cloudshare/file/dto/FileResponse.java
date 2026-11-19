package com.cloudshare.file.dto;

import com.cloudshare.file.FileStatus;
import com.cloudshare.file.StoredFile;

import java.time.Instant;
import java.util.UUID;

public class FileResponse {
    private UUID id;
    private String originalName;
    private String contentType;
    private Long sizeBytes;
    private FileStatus status;
    private long downloadCount;
    private Instant createdAt;

    public FileResponse() {}

    public FileResponse(UUID id, String originalName, String contentType, Long sizeBytes, FileStatus status, long downloadCount, Instant createdAt) {
        this.id = id;
        this.originalName = originalName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.status = status;
        this.downloadCount = downloadCount;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String originalName;
        private String contentType;
        private Long sizeBytes;
        private FileStatus status;
        private long downloadCount;
        private Instant createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder originalName(String originalName) { this.originalName = originalName; return this; }
        public Builder contentType(String contentType) { this.contentType = contentType; return this; }
        public Builder sizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; return this; }
        public Builder status(FileStatus status) { this.status = status; return this; }
        public Builder downloadCount(long downloadCount) { this.downloadCount = downloadCount; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public FileResponse build() {
            return new FileResponse(id, originalName, contentType, sizeBytes, status, downloadCount, createdAt);
        }
    }

    public static FileResponse fromEntity(StoredFile file, long downloadCount) {
        return new FileResponse(
                file.getId(),
                file.getOriginalName(),
                file.getContentType(),
                file.getSizeBytes(),
                file.getStatus(),
                downloadCount,
                file.getCreatedAt()
        );
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public FileStatus getStatus() { return status; }
    public void setStatus(FileStatus status) { this.status = status; }
    public long getDownloadCount() { return downloadCount; }
    public void setDownloadCount(long downloadCount) { this.downloadCount = downloadCount; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
