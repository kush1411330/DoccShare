package com.doccshare.file;

import com.doccshare.common.exception.BadRequestException;
import com.doccshare.common.exception.ResourceNotFoundException;
import com.doccshare.file.dto.FileResponse;
import com.doccshare.file.dto.RenameFileRequest;
import com.doccshare.file.dto.UploadUrlRequest;
import com.doccshare.file.dto.UploadUrlResponse;
import com.doccshare.share.ShareLinkRepository;
import com.doccshare.storage.StorageService;
import com.doccshare.user.User;
import com.doccshare.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
public class FileService {

    private static final Logger log = LoggerFactory.getLogger(FileService.class);

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final ShareLinkRepository shareLinkRepository;
    private final StorageService storageService;

    @Value("${doccshare.storage.max-upload-bytes:${MAX_UPLOAD_BYTES:104857600}}")
    private long maxUploadBytes;

    @Value("${doccshare.storage.upload-url-expiry-minutes:10}")
    private long uploadUrlExpiryMinutes;

    public FileService(
            FileRepository fileRepository,
            UserRepository userRepository,
            ShareLinkRepository shareLinkRepository,
            StorageService storageService
    ) {
        this.fileRepository = fileRepository;
        this.userRepository = userRepository;
        this.shareLinkRepository = shareLinkRepository;
        this.storageService = storageService;
    }

    @Transactional
    public UploadUrlResponse requestUploadUrl(UUID userId, UploadUrlRequest request) {
        if (request.getSizeBytes() > maxUploadBytes) {
            throw new BadRequestException("File size exceeds maximum allowed limit of " + (maxUploadBytes / (1024 * 1024)) + " MB");
        }

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UUID fileId = UUID.randomUUID();
        String sanitizedName = sanitizeFilename(request.getFileName());
        String storageKey = String.format("users/%s/%s/%s", userId, fileId, UUID.randomUUID());
        String contentType = (request.getContentType() != null && !request.getContentType().isBlank())
                ? request.getContentType()
                : "application/octet-stream";

        StoredFile file = StoredFile.builder()
                .id(fileId)
                .owner(owner)
                .originalName(sanitizedName)
                .storageKey(storageKey)
                .contentType(contentType)
                .sizeBytes(request.getSizeBytes())
                .status(FileStatus.PENDING)
                .build();

        StoredFile saved = fileRepository.save(file);
        UUID actualFileId = (saved != null && saved.getId() != null) ? saved.getId() : fileId;

        Map<String, Object> urlData = storageService.createUploadUrl(
                storageKey,
                contentType,
                request.getSizeBytes(),
                Duration.ofMinutes(uploadUrlExpiryMinutes)
        );

        @SuppressWarnings("unchecked")
        Map<String, String> headers = (Map<String, String>) urlData.get("headers");
        String uploadUrl = (String) urlData.get("uploadUrl");

        log.info("Issued upload URL for file: {} (id: {}) by user: {}", sanitizedName, actualFileId, userId);

        return UploadUrlResponse.builder()
                .fileId(actualFileId)
                .uploadUrl(uploadUrl)
                .headers(headers)
                .build();
    }

    @Transactional
    public FileResponse confirmUpload(UUID fileId, UUID userId) {
        StoredFile file = fileRepository.findByIdAndOwnerId(fileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        if (file.getStatus() == FileStatus.READY) {
            long downloadCount = shareLinkRepository.sumDownloadsByFileId(file.getId());
            return FileResponse.fromEntity(file, downloadCount);
        }

        // Verify with S3 HEAD
        long actualSize = storageService.headObject(file.getStorageKey());
        if (actualSize > maxUploadBytes) {
            storageService.deleteObject(file.getStorageKey());
            file.setStatus(FileStatus.DELETED);
            file = fileRepository.save(file);
            throw new BadRequestException("Uploaded file exceeds allowed size limit");
        }

        file.setSizeBytes(actualSize);
        file.setStatus(FileStatus.READY);
        file = fileRepository.save(file);

        log.info("Confirmed upload for file: {} (id: {}), size: {} bytes", file.getOriginalName(), fileId, actualSize);
        long downloadCount = shareLinkRepository.sumDownloadsByFileId(file.getId());
        return FileResponse.fromEntity(file, downloadCount);
    }

    @Transactional(readOnly = true)
    public Page<FileResponse> listFiles(UUID userId, Pageable pageable, String query) {
        Page<StoredFile> filesPage;
        if (query != null && !query.trim().isEmpty()) {
            filesPage = fileRepository.searchByOwnerIdAndStatus(userId, FileStatus.READY, query.trim(), pageable);
        } else {
            filesPage = fileRepository.findByOwnerIdAndStatus(userId, FileStatus.READY, pageable);
        }

        return filesPage.map(file -> {
            long downloadCount = shareLinkRepository.sumDownloadsByFileId(file.getId());
            return FileResponse.fromEntity(file, downloadCount);
        });
    }

    @Transactional(readOnly = true)
    public FileResponse getFile(UUID fileId, UUID userId) {
        StoredFile file = fileRepository.findByIdAndOwnerId(fileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        long downloadCount = shareLinkRepository.sumDownloadsByFileId(file.getId());
        return FileResponse.fromEntity(file, downloadCount);
    }

    @Transactional
    public FileResponse renameFile(UUID fileId, UUID userId, RenameFileRequest request) {
        StoredFile file = fileRepository.findByIdAndOwnerId(fileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        file.setOriginalName(sanitizeFilename(request.getFileName()));
        file = fileRepository.save(file);

        long downloadCount = shareLinkRepository.sumDownloadsByFileId(file.getId());
        return FileResponse.fromEntity(file, downloadCount);
    }

    @Transactional
    public void deleteFile(UUID fileId, UUID userId) {
        StoredFile file = fileRepository.findByIdAndOwnerId(fileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found"));

        storageService.deleteObject(file.getStorageKey());
        fileRepository.delete(file);
        log.info("Deleted file: {} (id: {}) by user: {}", file.getOriginalName(), fileId, userId);
    }

    private String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "unnamed_file";
        }
        String name = Paths.get(filename).getFileName().toString();
        name = name.replaceAll("[\\r\\n\\x00]", "").trim();
        return name.isEmpty() ? "unnamed_file" : name;
    }
}

