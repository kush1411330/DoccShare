package com.doccshare.analytics.dto;

public class AnalyticsOverviewDto {
    private long totalFiles;
    private long totalLinks;
    private long totalDownloads;
    private long uniqueVisitors30Days;
    private long downloads30Days;

    public AnalyticsOverviewDto() {}

    public AnalyticsOverviewDto(long totalFiles, long totalLinks, long totalDownloads, long uniqueVisitors30Days, long downloads30Days) {
        this.totalFiles = totalFiles;
        this.totalLinks = totalLinks;
        this.totalDownloads = totalDownloads;
        this.uniqueVisitors30Days = uniqueVisitors30Days;
        this.downloads30Days = downloads30Days;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long totalFiles;
        private long totalLinks;
        private long totalDownloads;
        private long uniqueVisitors30Days;
        private long downloads30Days;

        public Builder totalFiles(long totalFiles) { this.totalFiles = totalFiles; return this; }
        public Builder totalLinks(long totalLinks) { this.totalLinks = totalLinks; return this; }
        public Builder totalDownloads(long totalDownloads) { this.totalDownloads = totalDownloads; return this; }
        public Builder uniqueVisitors30Days(long uniqueVisitors30Days) { this.uniqueVisitors30Days = uniqueVisitors30Days; return this; }
        public Builder downloads30Days(long downloads30Days) { this.downloads30Days = downloads30Days; return this; }

        public AnalyticsOverviewDto build() {
            return new AnalyticsOverviewDto(totalFiles, totalLinks, totalDownloads, uniqueVisitors30Days, downloads30Days);
        }
    }

    public long getTotalFiles() { return totalFiles; }
    public void setTotalFiles(long totalFiles) { this.totalFiles = totalFiles; }
    public long getTotalLinks() { return totalLinks; }
    public void setTotalLinks(long totalLinks) { this.totalLinks = totalLinks; }
    public long getTotalDownloads() { return totalDownloads; }
    public void setTotalDownloads(long totalDownloads) { this.totalDownloads = totalDownloads; }
    public long getUniqueVisitors30Days() { return uniqueVisitors30Days; }
    public void setUniqueVisitors30Days(long uniqueVisitors30Days) { this.uniqueVisitors30Days = uniqueVisitors30Days; }
    public long getDownloads30Days() { return downloads30Days; }
    public void setDownloads30Days(long downloads30Days) { this.downloads30Days = downloads30Days; }
}

