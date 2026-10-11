package com.learnhub.service;

import com.learnhub.exception.BadRequestException;
import com.learnhub.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${app.upload.dir:./uploads}") String uploadDir) {
        this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.rootLocation);
        } catch (IOException e) {
            log.error("Could not initialize storage location", e);
        }
    }

    public String storeFile(MultipartFile file, String subDirectory) {
        if (file.isEmpty()) {
            throw new BadRequestException("Failed to store empty file");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        if (originalFilename.contains("..")) {
            throw new BadRequestException("Cannot store file with relative path outside directory: " + originalFilename);
        }

        String extension = "";
        int extIndex = originalFilename.lastIndexOf('.');
        if (extIndex > 0) {
            extension = originalFilename.substring(extIndex);
        }

        String storedFileName = UUID.randomUUID() + extension;
        Path targetDir = subDirectory != null ? this.rootLocation.resolve(subDirectory).normalize() : this.rootLocation;

        try {
            Files.createDirectories(targetDir);
            Path destinationFile = targetDir.resolve(storedFileName).normalize();

            if (!destinationFile.toAbsolutePath().startsWith(this.rootLocation.toAbsolutePath())) {
                throw new BadRequestException("Cannot store file outside current storage directory");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }

            return subDirectory != null ? subDirectory + "/" + storedFileName : storedFileName;
        } catch (IOException e) {
            log.error("Failed to store file: {}", e.getMessage());
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }
    }

    public Resource loadFileAsResource(String relativePath) {
        try {
            Path filePath = this.rootLocation.resolve(relativePath).normalize();
            if (!filePath.toAbsolutePath().startsWith(this.rootLocation.toAbsolutePath())) {
                throw new BadRequestException("Access denied for file path: " + relativePath);
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File not found or not readable: " + relativePath);
            }
        } catch (MalformedURLException e) {
            throw new ResourceNotFoundException("File not found: " + relativePath);
        }
    }
}
