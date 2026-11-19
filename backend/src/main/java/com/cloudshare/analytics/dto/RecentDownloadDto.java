package com.cloudshare.analytics.dto;

import com.cloudshare.analytics.DownloadEvent;

import java.time.Instant;
import java.util.UUID;

public class RecentDownloadDto {
    private UUID id;
    private Instant downloadedAt;
    private String countryCode;
    private String countryName;
    private String city;
    private String browser;
    private String os;
    private String deviceType;
    private String referrer;

    public RecentDownloadDto() {}

    public RecentDownloadDto(UUID id, Instant downloadedAt, String countryCode, String countryName, String city, String browser, String os, String deviceType, String referrer) {
        this.id = id;
        this.downloadedAt = downloadedAt;
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.city = city;
        this.browser = browser;
        this.os = os;
        this.deviceType = deviceType;
        this.referrer = referrer;
    }

    public static RecentDownloadDto fromEntity(DownloadEvent event) {
        return new RecentDownloadDto(
                event.getId(),
                event.getDownloadedAt(),
                event.getCountryCode(),
                event.getCountryName(),
                event.getCity(),
                event.getBrowser(),
                event.getOs(),
                event.getDeviceType(),
                event.getReferrer()
        );
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Instant getDownloadedAt() { return downloadedAt; }
    public void setDownloadedAt(Instant downloadedAt) { this.downloadedAt = downloadedAt; }
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
