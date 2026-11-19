package com.doccshare.storage;

import java.time.Duration;
import java.util.Map;

public interface StorageService {
    Map<String, Object> createUploadUrl(String key, String contentType, long size, Duration ttl);
    String createDownloadUrl(String key, String originalFilename, Duration ttl);
    long headObject(String key);
    void deleteObject(String key);
}

