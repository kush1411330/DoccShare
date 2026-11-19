package com.cloudshare.analytics.dto;

import java.util.List;

public class FileAnalyticsDto {
    private long totalDownloads;
    private long uniqueVisitors;
    private List<TimeSeriesDto> timeSeries;
    private List<GeoBreakdownDto> countries;
    private List<ItemBreakdownDto> browsers;
    private List<ItemBreakdownDto> operatingSystems;
    private List<ItemBreakdownDto> devices;
    private List<RecentDownloadDto> recent;

    public FileAnalyticsDto() {}

    public FileAnalyticsDto(long totalDownloads, long uniqueVisitors, List<TimeSeriesDto> timeSeries, List<GeoBreakdownDto> countries, List<ItemBreakdownDto> browsers, List<ItemBreakdownDto> operatingSystems, List<ItemBreakdownDto> devices, List<RecentDownloadDto> recent) {
        this.totalDownloads = totalDownloads;
        this.uniqueVisitors = uniqueVisitors;
        this.timeSeries = timeSeries;
        this.countries = countries;
        this.browsers = browsers;
        this.operatingSystems = operatingSystems;
        this.devices = devices;
        this.recent = recent;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long totalDownloads;
        private long uniqueVisitors;
        private List<TimeSeriesDto> timeSeries;
        private List<GeoBreakdownDto> countries;
        private List<ItemBreakdownDto> browsers;
        private List<ItemBreakdownDto> operatingSystems;
        private List<ItemBreakdownDto> devices;
        private List<RecentDownloadDto> recent;

        public Builder totalDownloads(long totalDownloads) { this.totalDownloads = totalDownloads; return this; }
        public Builder uniqueVisitors(long uniqueVisitors) { this.uniqueVisitors = uniqueVisitors; return this; }
        public Builder timeSeries(List<TimeSeriesDto> timeSeries) { this.timeSeries = timeSeries; return this; }
        public Builder countries(List<GeoBreakdownDto> countries) { this.countries = countries; return this; }
        public Builder browsers(List<ItemBreakdownDto> browsers) { this.browsers = browsers; return this; }
        public Builder operatingSystems(List<ItemBreakdownDto> operatingSystems) { this.operatingSystems = operatingSystems; return this; }
        public Builder devices(List<ItemBreakdownDto> devices) { this.devices = devices; return this; }
        public Builder recent(List<RecentDownloadDto> recent) { this.recent = recent; return this; }

        public FileAnalyticsDto build() {
            return new FileAnalyticsDto(totalDownloads, uniqueVisitors, timeSeries, countries, browsers, operatingSystems, devices, recent);
        }
    }

    public long getTotalDownloads() { return totalDownloads; }
    public void setTotalDownloads(long totalDownloads) { this.totalDownloads = totalDownloads; }
    public long getUniqueVisitors() { return uniqueVisitors; }
    public void setUniqueVisitors(long uniqueVisitors) { this.uniqueVisitors = uniqueVisitors; }
    public List<TimeSeriesDto> getTimeSeries() { return timeSeries; }
    public void setTimeSeries(List<TimeSeriesDto> timeSeries) { this.timeSeries = timeSeries; }
    public List<GeoBreakdownDto> getCountries() { return countries; }
    public void setCountries(List<GeoBreakdownDto> countries) { this.countries = countries; }
    public List<ItemBreakdownDto> getBrowsers() { return browsers; }
    public void setBrowsers(List<ItemBreakdownDto> browsers) { this.browsers = browsers; }
    public List<ItemBreakdownDto> getOperatingSystems() { return operatingSystems; }
    public void setOperatingSystems(List<ItemBreakdownDto> operatingSystems) { this.operatingSystems = operatingSystems; }
    public List<ItemBreakdownDto> getDevices() { return devices; }
    public void setDevices(List<ItemBreakdownDto> devices) { this.devices = devices; }
    public List<RecentDownloadDto> getRecent() { return recent; }
    public void setRecent(List<RecentDownloadDto> recent) { this.recent = recent; }
}
