package com.cloudshare.file;

import com.cloudshare.file.dto.FileResponse;
import com.cloudshare.file.dto.RenameFileRequest;
import com.cloudshare.file.dto.UploadUrlRequest;
import com.cloudshare.file.dto.UploadUrlResponse;
import com.cloudshare.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Files", description = "Endpoints for file uploads, listings, renaming, and deletion")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload-url")
    @Operation(summary = "Generate a presigned direct-to-storage upload URL")
    public ResponseEntity<UploadUrlResponse> requestUploadUrl(@Valid @RequestBody UploadUrlRequest request) {
        UploadUrlResponse response = fileService.requestUploadUrl(SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm successful upload to object storage and activate file")
    public ResponseEntity<FileResponse> confirmUpload(@PathVariable("id") UUID id) {
        FileResponse response = fileService.confirmUpload(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List own files with pagination, sorting, and search filter")
    public ResponseEntity<Page<FileResponse>> listFiles(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sort", defaultValue = "createdAt,desc") String sort,
            @RequestParam(name = "q", required = false) String query
    ) {
        String[] sortParts = sort.split(",");
        String sortProp = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProp));

        Page<FileResponse> files = fileService.listFiles(SecurityUtils.getCurrentUserId(), pageable, query);
        return ResponseEntity.ok(files);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get file details by ID")
    public ResponseEntity<FileResponse> getFile(@PathVariable("id") UUID id) {
        FileResponse response = fileService.getFile(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Rename a file")
    public ResponseEntity<FileResponse> renameFile(
            @PathVariable("id") UUID id,
            @Valid @RequestBody RenameFileRequest request
    ) {
        FileResponse response = fileService.renameFile(id, SecurityUtils.getCurrentUserId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a file from database and cloud storage")
    public ResponseEntity<Void> deleteFile(@PathVariable("id") UUID id) {
        fileService.deleteFile(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
