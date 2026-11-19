package com.doccshare.share;

import com.doccshare.security.SecurityUtils;
import com.doccshare.share.dto.CreateShareRequest;
import com.doccshare.share.dto.ShareResponse;
import com.doccshare.share.dto.UpdateShareRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Share Links", description = "Endpoints for creating and managing secure share links")
public class ShareController {

    private final ShareService shareService;

    public ShareController(ShareService shareService) {
        this.shareService = shareService;
    }

    @PostMapping("/api/files/{id}/shares")
    @Operation(summary = "Create a new share link for a file")
    public ResponseEntity<ShareResponse> createShareLink(
            @PathVariable("id") UUID fileId,
            @Valid @RequestBody CreateShareRequest request
    ) {
        ShareResponse response = shareService.createShareLink(fileId, SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/files/{id}/shares")
    @Operation(summary = "List all share links for a file")
    public ResponseEntity<List<ShareResponse>> listShareLinks(@PathVariable("id") UUID fileId) {
        List<ShareResponse> response = shareService.listShareLinks(fileId, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/shares/{shareId}")
    @Operation(summary = "Update share link settings (enable/disable, password, expiry, limits)")
    public ResponseEntity<ShareResponse> updateShareLink(
            @PathVariable("shareId") UUID shareId,
            @Valid @RequestBody UpdateShareRequest request
    ) {
        ShareResponse response = shareService.updateShareLink(shareId, SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/shares/{shareId}")
    @Operation(summary = "Revoke and permanently delete a share link")
    public ResponseEntity<Void> revokeShareLink(@PathVariable("shareId") UUID shareId) {
        shareService.revokeShareLink(shareId, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}

