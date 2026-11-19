package com.cloudshare.share.dto;

import com.cloudshare.share.ShareLink;

import java.time.Instant;
import java.util.UUID;

public class ShareResponse {
    private UUID id;
    private UUID fileId;
    private String token;
    private String publicUrl;
    private boolean passwordProtected;
    private Instant expiresAt;
    private Integer maxDownloads;
    private int downloadCount;
    private boolean active;
    private boolean expired;
    private Instant createdAt;

    public ShareResponse() {}

    public ShareResponse(UUID id, UUID fileId, String token, String publicUrl, boolean passwordProtected, Instant expiresAt, Integer maxDownloads, int downloadCount, boolean active, boolean expired, Instant createdAt) {
        this.id = id;
        this.fileId = fileId;
        this.token = token;
        this.publicUrl = publicUrl;
        this.passwordProtected = passwordProtected;
        this.expiresAt = expiresAt;
        this.maxDownloads = maxDownloads;
        this.downloadCount = downloadCount;
        this.active = active;
        this.expired = expired;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private UUID fileId;
        private String token;
        private String publicUrl;
        private boolean passwordProtected;
        private Instant expiresAt;
        private Integer maxDownloads;
        private int downloadCount;
        private boolean active;
        private boolean expired;
        private Instant createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder fileId(UUID fileId) { this.fileId = fileId; return this; }
        public Builder token(String token) { this.token = token; return this; }
        public Builder publicUrl(String publicUrl) { this.publicUrl = publicUrl; return this; }
        public Builder passwordProtected(boolean passwordProtected) { this.passwordProtected = passwordProtected; return this; }
        public Builder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder maxDownloads(Integer maxDownloads) { this.maxDownloads = maxDownloads; return this; }
        public Builder downloadCount(int downloadCount) { this.downloadCount = downloadCount; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder expired(boolean expired) { this.expired = expired; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public ShareResponse build() {
            return new ShareResponse(id, fileId, token, publicUrl, passwordProtected, expiresAt, maxDownloads, downloadCount, active, expired, createdAt);
        }
    }

    public static ShareResponse fromEntity(ShareLink link, String publicBaseUrl) {
        boolean isExpired = link.getExpiresAt() != null && link.getExpiresAt().isBefore(Instant.now());
        String baseUrl = publicBaseUrl.endsWith("/") ? publicBaseUrl.substring(0, publicBaseUrl.length() - 1) : publicBaseUrl;

        return new ShareResponse(
                link.getId(),
                link.getFile().getId(),
                link.getToken(),
                baseUrl + "/s/" + link.getToken(),
                link.getPasswordHash() != null,
                link.getExpiresAt(),
                link.getMaxDownloads(),
                link.getDownloadCount(),
                link.isActive(),
                isExpired,
                link.getCreatedAt()
        );
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getFileId() { return fileId; }
    public void setFileId(UUID fileId) { this.fileId = fileId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getPublicUrl() { return publicUrl; }
    public void setPublicUrl(String publicUrl) { this.publicUrl = publicUrl; }
    public boolean isPasswordProtected() { return passwordProtected; }
    public void setPasswordProtected(boolean passwordProtected) { this.passwordProtected = passwordProtected; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Integer getMaxDownloads() { return maxDownloads; }
    public void setMaxDownloads(Integer maxDownloads) { this.maxDownloads = maxDownloads; }
    public int getDownloadCount() { return downloadCount; }
    public void setDownloadCount(int downloadCount) { this.downloadCount = downloadCount; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
