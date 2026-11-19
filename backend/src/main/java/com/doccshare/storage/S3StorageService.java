package com.doccshare.storage;

import com.doccshare.common.exception.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import jakarta.annotation.PostConstruct;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class S3StorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${DoccShare.s3.bucket:DoccShare-files}")
    private String bucket;

    public S3StorageService(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @PostConstruct
    public void initBucket() {
        try {
            try {
                s3Client.headBucket(HeadBucketRequest.builder().bucket(bucket).build());
                log.info("S3 bucket '{}' verified.", bucket);
            } catch (NoSuchBucketException e) {
                log.info("S3 bucket '{}' not found, creating bucket...", bucket);
                s3Client.createBucket(CreateBucketRequest.builder().bucket(bucket).build());
            }

            CORSRule corsRule = CORSRule.builder()
                    .allowedOrigins("*")
                    .allowedMethods("GET", "PUT", "POST", "DELETE", "HEAD")
                    .allowedHeaders("*")
                    .exposeHeaders("ETag")
                    .maxAgeSeconds(3600)
                    .build();

            CORSConfiguration corsConfig = CORSConfiguration.builder()
                    .corsRules(List.of(corsRule))
                    .build();

            s3Client.putBucketCors(PutBucketCorsRequest.builder()
                    .bucket(bucket)
                    .corsConfiguration(corsConfig)
                    .build());
            log.info("S3 bucket '{}' CORS policy applied successfully.", bucket);
        } catch (Exception e) {
            log.warn("Could not automatically configure bucket/CORS on '{}': {}", bucket, e.getMessage());
        }
    }

    @Override
    public Map<String, Object> createUploadUrl(String key, String contentType, long size, Duration ttl) {
        try {
            // Do not bind contentType into the SigV4 signature so browsers can stream PUT
            // without signature mismatch caused by charset or client header variations.
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(ttl)
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
            String url = presigned.url().toString();

            Map<String, String> headers = new HashMap<>();
            if (contentType != null && !contentType.isBlank()) {
                headers.put("Content-Type", contentType);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("uploadUrl", url);
            result.put("headers", headers);
            return result;
        } catch (Exception e) {
            log.error("Failed to generate presigned upload URL for key {}: {}", key, e.getMessage(), e);
            throw new StorageException("Failed to generate upload URL: " + e.getMessage(), e);
        }
    }

    @Override
    public String createDownloadUrl(String key, String originalFilename, Duration ttl) {
        try {
            String safeFilename = originalFilename != null ? originalFilename.replaceAll("[\\r\\n\"]", "_") : "download";
            String encodedFilename = URLEncoder.encode(safeFilename, StandardCharsets.UTF_8).replace("+", "%20");
            String contentDisposition = String.format("attachment; filename=\"%s\"; filename*=UTF-8''%s", safeFilename, encodedFilename);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .responseContentDisposition(contentDisposition)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(ttl)
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
            return presigned.url().toString();
        } catch (Exception e) {
            log.error("Failed to generate presigned download URL for key {}: {}", key, e.getMessage(), e);
            throw new StorageException("Failed to generate download URL: " + e.getMessage(), e);
        }
    }

    @Override
    public long headObject(String key) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            HeadObjectResponse response = s3Client.headObject(request);
            return response.contentLength();
        } catch (NoSuchKeyException e) {
            log.warn("Storage object not found for key: {}", key);
            throw new StorageException("Uploaded object not found in storage");
        } catch (Exception e) {
            log.error("Failed to check storage object for key {}: {}", key, e.getMessage(), e);
            throw new StorageException("Failed to verify uploaded object: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteObject(String key) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
            log.info("Deleted storage object with key: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete object from storage {}: {}", key, e.getMessage(), e);
        }
    }
}

