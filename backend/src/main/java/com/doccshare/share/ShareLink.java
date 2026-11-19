package com.doccshare.share;

import com.doccshare.file.StoredFile;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "share_links")
public class ShareLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id", nullable = false)
    private StoredFile file;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "max_downloads")
    private Integer maxDownloads;

    @Column(name = "download_count", nullable = false)
    private int downloadCount = 0;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ShareLink() {}

    public ShareLink(UUID id, StoredFile file, String token, String passwordHash, Instant expiresAt, Integer maxDownloads, int downloadCount, boolean active, Instant createdAt) {
        this.id = id;
        this.file = file;
        this.token = token;
        this.passwordHash = passwordHash;
        this.expiresAt = expiresAt;
        this.maxDownloads = maxDownloads;
        this.downloadCount = downloadCount;
        this.active = active;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private StoredFile file;
        private String token;
        private String passwordHash;
        private Instant expiresAt;
        private Integer maxDownloads;
        private int downloadCount = 0;
        private boolean active = true;
        private Instant createdAt;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder file(StoredFile file) { this.file = file; return this; }
        public Builder token(String token) { this.token = token; return this; }
        public Builder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public Builder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }
        public Builder maxDownloads(Integer maxDownloads) { this.maxDownloads = maxDownloads; return this; }
        public Builder downloadCount(int downloadCount) { this.downloadCount = downloadCount; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }

        public ShareLink build() {
            return new ShareLink(id, file, token, passwordHash, expiresAt, maxDownloads, downloadCount, active, createdAt);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public StoredFile getFile() { return file; }
    public void setFile(StoredFile file) { this.file = file; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Integer getMaxDownloads() { return maxDownloads; }
    public void setMaxDownloads(Integer maxDownloads) { this.maxDownloads = maxDownloads; }
    public int getDownloadCount() { return downloadCount; }
    public void setDownloadCount(int downloadCount) { this.downloadCount = downloadCount; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

