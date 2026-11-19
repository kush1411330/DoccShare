package com.cloudshare.share;

import com.cloudshare.share.dto.DownloadRequest;
import com.cloudshare.share.dto.DownloadResponse;
import com.cloudshare.share.dto.PublicShareMetadata;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/shares")
@Tag(name = "Public Downloads", description = "Public unauthenticated endpoints for accessing shared files")
public class PublicShareController {

    private final ShareService shareService;

    public PublicShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @GetMapping("/{token}")
    @Operation(summary = "Get public metadata for a shared file")
    public ResponseEntity<PublicShareMetadata> getMetadata(@PathVariable("token") String token) {
        PublicShareMetadata metadata = shareService.getPublicShareMetadata(token);
        return ResponseEntity.ok(metadata);
    }

    @PostMapping("/{token}/download")
    @Operation(summary = "Request secure presigned download URL for a shared file")
    public ResponseEntity<DownloadResponse> download(
            @PathVariable("token") String token,
            @RequestBody(required = false) DownloadRequest request,
            HttpServletRequest httpRequest
    ) {
        DownloadResponse response = shareService.processDownload(token, request, httpRequest);
        return ResponseEntity.ok(response);
    }
}
