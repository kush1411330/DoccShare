package com.doccshare.share.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class CreateShareRequest {

    @Size(max = 100, message = "Password cannot exceed 100 characters")
    private String password;

    @Future(message = "Expiry date must be in the future")
    private Instant expiresAt;

    @Positive(message = "Max downloads must be greater than zero")
    private Integer maxDownloads;

    public CreateShareRequest() {}

    public CreateShareRequest(String password, Instant expiresAt, Integer maxDownloads) {
        this.password = password;
        this.expiresAt = expiresAt;
        this.maxDownloads = maxDownloads;
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Integer getMaxDownloads() { return maxDownloads; }
    public void setMaxDownloads(Integer maxDownloads) { this.maxDownloads = maxDownloads; }
}

