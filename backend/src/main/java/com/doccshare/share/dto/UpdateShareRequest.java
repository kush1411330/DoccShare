package com.doccshare.share.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class UpdateShareRequest {

    private Boolean active;

    @Size(max = 100, message = "Password cannot exceed 100 characters")
    private String password;

    private Boolean clearPassword;

    @Future(message = "Expiry date must be in the future")
    private Instant expiresAt;

    private Boolean clearExpiry;

    @Positive(message = "Max downloads must be greater than zero")
    private Integer maxDownloads;

    private Boolean clearMaxDownloads;

    public UpdateShareRequest() {}

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Boolean getClearPassword() { return clearPassword; }
    public void setClearPassword(Boolean clearPassword) { this.clearPassword = clearPassword; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Boolean getClearExpiry() { return clearExpiry; }
    public void setClearExpiry(Boolean clearExpiry) { this.clearExpiry = clearExpiry; }
    public Integer getMaxDownloads() { return maxDownloads; }
    public void setMaxDownloads(Integer maxDownloads) { this.maxDownloads = maxDownloads; }
    public Boolean getClearMaxDownloads() { return clearMaxDownloads; }
    public void setClearMaxDownloads(Boolean clearMaxDownloads) { this.clearMaxDownloads = clearMaxDownloads; }
}

