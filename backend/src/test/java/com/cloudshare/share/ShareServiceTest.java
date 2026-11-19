package com.cloudshare.share;

import com.cloudshare.analytics.AnalyticsService;
import com.cloudshare.common.exception.BadRequestException;
import com.cloudshare.common.exception.ResourceNotFoundException;
import com.cloudshare.common.exception.UnauthorizedException;
import com.cloudshare.file.FileRepository;
import com.cloudshare.file.FileStatus;
import com.cloudshare.file.StoredFile;
import com.cloudshare.share.dto.CreateShareRequest;
import com.cloudshare.share.dto.DownloadRequest;
import com.cloudshare.share.dto.DownloadResponse;
import com.cloudshare.share.dto.ShareResponse;
import com.cloudshare.storage.StorageService;
import com.cloudshare.user.User;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShareServiceTest {

    @Mock
    private ShareLinkRepository shareLinkRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private AnalyticsService analyticsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private ShareService shareService;

    private User owner;
    private StoredFile file;
    private ShareLink link;
    private final UUID userId = UUID.randomUUID();
    private final UUID fileId = UUID.randomUUID();
    private final UUID linkId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(shareService, "publicBaseUrl", "http://localhost:4200");
        ReflectionTestUtils.setField(shareService, "downloadUrlExpirySeconds", 60L);

        owner = User.builder().id(userId).email("user@example.com").build();
        file = StoredFile.builder()
                .id(fileId)
                .owner(owner)
                .originalName("document.pdf")
                .storageKey("users/" + userId + "/" + fileId + "/abc")
                .contentType("application/pdf")
                .sizeBytes(1024L)
                .status(FileStatus.READY)
                .build();

        link = ShareLink.builder()
                .id(linkId)
                .file(file)
                .token("valid-test-token-123456789012345")
                .active(true)
                .downloadCount(0)
                .build();
    }

    @Test
    void createShareLink_success() {
        when(fileRepository.findByIdAndOwnerId(fileId, userId)).thenReturn(Optional.of(file));
        when(shareLinkRepository.save(any(ShareLink.class))).thenAnswer(i -> {
            ShareLink saved = i.getArgument(0);
            saved.setId(linkId);
            return saved;
        });

        CreateShareRequest request = new CreateShareRequest("secret123", Instant.now().plus(7, ChronoUnit.DAYS), 5);
        when(passwordEncoder.encode("secret123")).thenReturn("hashed-password");

        ShareResponse response = shareService.createShareLink(fileId, userId, request);

        assertNotNull(response);
        assertEquals(fileId, response.getFileId());
        assertTrue(response.isPasswordProtected());
        assertTrue(response.getPublicUrl().contains("/s/"));
        verify(shareLinkRepository).save(any(ShareLink.class));
    }

    @Test
    void processDownload_success() {
        when(shareLinkRepository.findByToken("valid-token")).thenReturn(Optional.of(link));
        when(shareLinkRepository.incrementDownloadCountSafely(eq(linkId), any(Instant.class))).thenReturn(1);
        when(storageService.createDownloadUrl(eq(file.getStorageKey()), eq(file.getOriginalName()), any(Duration.class)))
                .thenReturn("https://storage.example.com/download?presigned=true");
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        DownloadResponse response = shareService.processDownload("valid-token", null, httpRequest);

        assertNotNull(response);
        assertEquals("https://storage.example.com/download?presigned=true", response.getDownloadUrl());
        verify(analyticsService).recordDownload(eq(linkId), eq(fileId), any(), any(), any());
    }

    @Test
    void processDownload_expiredLink_throwsException() {
        link.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS)); // expired
        when(shareLinkRepository.findByToken("expired-token")).thenReturn(Optional.of(link));

        assertThrows(BadRequestException.class, () ->
                shareService.processDownload("expired-token", null, httpRequest));
    }

    @Test
    void processDownload_maxDownloadsReached_throwsException() {
        link.setMaxDownloads(2);
        link.setDownloadCount(2);
        when(shareLinkRepository.findByToken("maxed-token")).thenReturn(Optional.of(link));

        assertThrows(BadRequestException.class, () ->
                shareService.processDownload("maxed-token", null, httpRequest));
    }

    @Test
    void processDownload_wrongPassword_throwsException() {
        link.setPasswordHash("hashed-pw");
        when(shareLinkRepository.findByToken("pw-token")).thenReturn(Optional.of(link));
        when(passwordEncoder.matches("wrong-pw", "hashed-pw")).thenReturn(false);

        DownloadRequest req = new DownloadRequest("wrong-pw");
        assertThrows(UnauthorizedException.class, () ->
                shareService.processDownload("pw-token", req, httpRequest));
    }

    @Test
    void processDownload_raceConditionPrevented_whenAtomicIncrementReturnsZero() {
        when(shareLinkRepository.findByToken("race-token")).thenReturn(Optional.of(link));
        // Atomic increment fails because another thread hit the limit simultaneously
        when(shareLinkRepository.incrementDownloadCountSafely(eq(linkId), any(Instant.class))).thenReturn(0);

        assertThrows(BadRequestException.class, () ->
                shareService.processDownload("race-token", null, httpRequest));
    }
}
