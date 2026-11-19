package com.doccshare.share.dto;

public class DownloadRequest {
    private String password;

    public DownloadRequest() {}

    public DownloadRequest(String password) {
        this.password = password;
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

