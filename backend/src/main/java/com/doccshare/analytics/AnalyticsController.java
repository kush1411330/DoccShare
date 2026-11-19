package com.doccshare.analytics;

import com.doccshare.analytics.dto.AnalyticsOverviewDto;
import com.doccshare.analytics.dto.FileAnalyticsDto;
import com.doccshare.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Analytics", description = "Endpoints for download analytics and user metrics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Get overall analytics overview across all user files")
    public ResponseEntity<AnalyticsOverviewDto> getOverview() {
        AnalyticsOverviewDto overview = analyticsService.getOverview(SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/files/{id}")
    @Operation(summary = "Get aggregated analytics for a specific file")
    public ResponseEntity<FileAnalyticsDto> getFileAnalytics(
            @PathVariable("id") UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        FileAnalyticsDto analytics = analyticsService.getFileAnalytics(id, SecurityUtils.getCurrentUserId(), from, to);
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/shares/{shareId}")
    @Operation(summary = "Get aggregated analytics for a specific share link")
    public ResponseEntity<FileAnalyticsDto> getShareLinkAnalytics(
            @PathVariable("shareId") UUID shareId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        FileAnalyticsDto analytics = analyticsService.getShareLinkAnalytics(shareId, SecurityUtils.getCurrentUserId(), from, to);
        return ResponseEntity.ok(analytics);
    }
}

