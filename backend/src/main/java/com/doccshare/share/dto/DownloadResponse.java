package com.doccshare.share.dto;

public class DownloadResponse {
    private String downloadUrl;
    private long expiresInSeconds;

    public DownloadResponse() {}

    public DownloadResponse(String downloadUrl, long expiresInSeconds) {
        this.downloadUrl = downloadUrl;
        this.expiresInSeconds = expiresInSeconds;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String downloadUrl;
        private long expiresInSeconds;

        public Builder downloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; return this; }
        public Builder expiresInSeconds(long expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; return this; }

        public DownloadResponse build() {
            return new DownloadResponse(downloadUrl, expiresInSeconds);
        }
    }

    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    public long getExpiresInSeconds() { return expiresInSeconds; }
    public void setExpiresInSeconds(long expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; }
}

