package com.cloudshare.analytics;

import com.cloudshare.file.StoredFile;
import com.cloudshare.share.ShareLink;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "download_events")
public class DownloadEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "share_link_id", nullable = false)
    private ShareLink shareLink;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id", nullable = false)
    private StoredFile file;

    @CreationTimestamp
    @Column(name = "downloaded_at", nullable = false, updatable = false)
    private Instant downloadedAt;

    @Column(name = "ip_hash", length = 64)
    private String ipHash;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "country_name", length = 100)
    private String countryName;

    @Column(length = 100)
    private String city;

    @Column(length = 80)
    private String browser;

    @Column(length = 80)
    private String os;

    @Column(name = "device_type", length = 20)
    private String deviceType;

    @Column(length = 512)
    private String referrer;

    public DownloadEvent() {}

    public DownloadEvent(UUID id, ShareLink shareLink, StoredFile file, Instant downloadedAt, String ipHash, String countryCode, String countryName, String city, String browser, String os, String deviceType, String referrer) {
        this.id = id;
        this.shareLink = shareLink;
        this.file = file;
        this.downloadedAt = downloadedAt;
        this.ipHash = ipHash;
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.city = city;
        this.browser = browser;
        this.os = os;
        this.deviceType = deviceType;
        this.referrer = referrer;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private ShareLink shareLink;
        private StoredFile file;
        private Instant downloadedAt;
        private String ipHash;
        private String countryCode;
        private String countryName;
        private String city;
        private String browser;
        private String os;
        private String deviceType;
        private String referrer;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder shareLink(ShareLink shareLink) { this.shareLink = shareLink; return this; }
        public Builder file(StoredFile file) { this.file = file; return this; }
        public Builder downloadedAt(Instant downloadedAt) { this.downloadedAt = downloadedAt; return this; }
        public Builder ipHash(String ipHash) { this.ipHash = ipHash; return this; }
        public Builder countryCode(String countryCode) { this.countryCode = countryCode; return this; }
        public Builder countryName(String countryName) { this.countryName = countryName; return this; }
        public Builder city(String city) { this.city = city; return this; }
        public Builder browser(String browser) { this.browser = browser; return this; }
        public Builder os(String os) { this.os = os; return this; }
        public Builder deviceType(String deviceType) { this.deviceType = deviceType; return this; }
        public Builder referrer(String referrer) { this.referrer = referrer; return this; }

        public DownloadEvent build() {
            return new DownloadEvent(id, shareLink, file, downloadedAt, ipHash, countryCode, countryName, city, browser, os, deviceType, referrer);
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ShareLink getShareLink() { return shareLink; }
    public void setShareLink(ShareLink shareLink) { this.shareLink = shareLink; }
    public StoredFile getFile() { return file; }
    public void setFile(StoredFile file) { this.file = file; }
    public Instant getDownloadedAt() { return downloadedAt; }
    public void setDownloadedAt(Instant downloadedAt) { this.downloadedAt = downloadedAt; }
    public String getIpHash() { return ipHash; }
    public void setIpHash(String ipHash) { this.ipHash = ipHash; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }
    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    public String getReferrer() { return referrer; }
    public void setReferrer(String referrer) { this.referrer = referrer; }
}
