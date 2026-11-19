package com.doccshare.file.dto;

import java.util.Map;
import java.util.UUID;

public class UploadUrlResponse {
    private UUID fileId;
    private String uploadUrl;
    private Map<String, String> headers;

    public UploadUrlResponse() {}

    public UploadUrlResponse(UUID fileId, String uploadUrl, Map<String, String> headers) {
        this.fileId = fileId;
        this.uploadUrl = uploadUrl;
        this.headers = headers;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID fileId;
        private String uploadUrl;
        private Map<String, String> headers;

        public Builder fileId(UUID fileId) { this.fileId = fileId; return this; }
        public Builder uploadUrl(String uploadUrl) { this.uploadUrl = uploadUrl; return this; }
        public Builder headers(Map<String, String> headers) { this.headers = headers; return this; }

        public UploadUrlResponse build() {
            return new UploadUrlResponse(fileId, uploadUrl, headers);
        }
    }

    public UUID getFileId() { return fileId; }
    public void setFileId(UUID fileId) { this.fileId = fileId; }
    public String getUploadUrl() { return uploadUrl; }
    public void setUploadUrl(String uploadUrl) { this.uploadUrl = uploadUrl; }
    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
}

