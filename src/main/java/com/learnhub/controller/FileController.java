package com.learnhub.controller;

import com.learnhub.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLConnection;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "Endpoints for local file upload and resource streaming")
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @Operation(summary = "Upload a file locally (video, attachment, submission)")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "general") String category
    ) {
        String filePath = fileStorageService.storeFile(file, category);
        return ResponseEntity.ok(Map.of(
                "filePath", filePath,
                "originalName", file.getOriginalFilename() != null ? file.getOriginalFilename() : "file",
                "fileUrl", "/api/files/" + filePath
        ));
    }

    @GetMapping("/{category}/{fileName:.+}")
    @Operation(summary = "Download or stream a locally stored file")
    public ResponseEntity<Resource> getFile(
            @PathVariable String category,
            @PathVariable String fileName
    ) {
        String relativePath = category + "/" + fileName;
        Resource resource = fileStorageService.loadFileAsResource(relativePath);

        String contentType = null;
        try {
            contentType = URLConnection.guessContentTypeFromName(resource.getFilename());
        } catch (Exception ignored) {}

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
