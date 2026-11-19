package com.cloudshare.file;

import com.cloudshare.common.exception.BadRequestException;
import com.cloudshare.common.exception.ResourceNotFoundException;
import com.cloudshare.file.dto.FileResponse;
import com.cloudshare.file.dto.RenameFileRequest;
import com.cloudshare.file.dto.UploadUrlRequest;
import com.cloudshare.file.dto.UploadUrlResponse;
import com.cloudshare.share.ShareLinkRepository;
import com.cloudshare.storage.StorageService;
import com.cloudshare.user.User;
import com.cloudshare.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShareLinkRepository shareLinkRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private FileService fileService;

    private final UUID userId = UUID.randomUUID();
    private final UUID fileId = UUID.randomUUID();
    private User user;
    private StoredFile file;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "maxUploadBytes", 104857600L); // 100MB
        ReflectionTestUtils.setField(fileService, "uploadUrlExpiryMinutes", 10L);

        user = User.builder().id(userId).email("user@example.com").build();
        file = StoredFile.builder()
                .id(fileId)
                .owner(user)
                .originalName("test.pdf")
                .storageKey("users/" + userId + "/" + fileId + "/abc")
                .contentType("application/pdf")
                .sizeBytes(2048L)
                .status(FileStatus.PENDING)
                .build();
    }

    @Test
    void requestUploadUrl_success() {
        UploadUrlRequest request = new UploadUrlRequest("test.pdf", "application/pdf", 2048L);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storageService.createUploadUrl(any(), eq("application/pdf"), eq(2048L), any(Duration.class)))
                .thenReturn(Map.of("uploadUrl", "https://s3.example.com/upload", "headers", Map.of("Content-Type", "application/pdf")));

        UploadUrlResponse response = fileService.requestUploadUrl(userId, request);

        assertNotNull(response);
        assertNotNull(response.getFileId());
        assertEquals("https://s3.example.com/upload", response.getUploadUrl());
        verify(fileRepository).save(any(StoredFile.class));
    }

    @Test
    void requestUploadUrl_exceedsMaxSize_throwsBadRequest() {
        UploadUrlRequest request = new UploadUrlRequest("huge.iso", "application/octet-stream", 200_000_000L); // 200MB

        assertThrows(BadRequestException.class, () -> fileService.requestUploadUrl(userId, request));
        verify(fileRepository, never()).save(any());
    }

    @Test
    void confirmUpload_success() {
        when(fileRepository.findByIdAndOwnerId(fileId, userId)).thenReturn(Optional.of(file));
        when(storageService.headObject(file.getStorageKey())).thenReturn(2048L);
        when(fileRepository.save(any(StoredFile.class))).thenAnswer(i -> i.getArgument(0));
        when(shareLinkRepository.sumDownloadsByFileId(fileId)).thenReturn(0L);

        FileResponse response = fileService.confirmUpload(fileId, userId);

        assertNotNull(response);
        assertEquals(FileStatus.READY, response.getStatus());
        assertEquals(2048L, response.getSizeBytes());
    }

    @Test
    void renameFile_success() {
        when(fileRepository.findByIdAndOwnerId(fileId, userId)).thenReturn(Optional.of(file));
        when(fileRepository.save(any(StoredFile.class))).thenAnswer(i -> i.getArgument(0));

        FileResponse response = fileService.renameFile(fileId, userId, new RenameFileRequest("new-name.pdf"));

        assertNotNull(response);
        assertEquals("new-name.pdf", response.getOriginalName());
    }

    @Test
    void deleteFile_success() {
        when(fileRepository.findByIdAndOwnerId(fileId, userId)).thenReturn(Optional.of(file));

        fileService.deleteFile(fileId, userId);

        verify(storageService).deleteObject(file.getStorageKey());
        verify(fileRepository).delete(file);
    }
}
