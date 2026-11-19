package com.cloudshare.analytics.dto;

public class GeoBreakdownDto {
    private String countryCode;
    private String countryName;
    private long count;

    public GeoBreakdownDto() {}

    public GeoBreakdownDto(String countryCode, String countryName, long count) {
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.count = count;
    }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
