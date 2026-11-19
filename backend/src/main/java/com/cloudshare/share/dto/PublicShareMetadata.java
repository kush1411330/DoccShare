package com.cloudshare.share.dto;

import java.time.Instant;

public class PublicShareMetadata {
    private String fileName;
    private Long sizeBytes;
    private String contentType;
    private boolean passwordRequired;
    private boolean expired;
    private boolean maxDownloadsReached;
    private Instant expiresAt;

    public PublicShareMetadata() {}

    public PublicShareMetadata(String fileName, Long sizeBytes, String contentType, boolean passwordRequired, boolean expired, boolean maxDownloadsReached, Instant expiresAt) {
        this.fileName = fileName;
        this.sizeBytes = sizeBytes;
        this.contentType = contentType;
        this.passwordRequired = passwordRequired;
        this.expired = expired;
        this.maxDownloadsReached = maxDownloadsReached;
        this.expiresAt = expiresAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String fileName;
        private Long sizeBytes;
        private String contentType;
        private boolean passwordRequired;
        private boolean expired;
        private boolean maxDownloadsReached;
        private Instant expiresAt;

        public Builder fileName(String fileName) { this.fileName = fileName; return this; }
        public Builder sizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; return this; }
        public Builder contentType(String contentType) { this.contentType = contentType; return this; }
        public Builder passwordRequired(boolean passwordRequired) { this.passwordRequired = passwordRequired; return this; }
        public Builder expired(boolean expired) { this.expired = expired; return this; }
        public Builder maxDownloadsReached(boolean maxDownloadsReached) { this.maxDownloadsReached = maxDownloadsReached; return this; }
        public Builder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }

        public PublicShareMetadata build() {
            return new PublicShareMetadata(fileName, sizeBytes, contentType, passwordRequired, expired, maxDownloadsReached, expiresAt);
        }
    }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public boolean isPasswordRequired() { return passwordRequired; }
    public void setPasswordRequired(boolean passwordRequired) { this.passwordRequired = passwordRequired; }
    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }
    public boolean isMaxDownloadsReached() { return maxDownloadsReached; }
    public void setMaxDownloadsReached(boolean maxDownloadsReached) { this.maxDownloadsReached = maxDownloadsReached; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
