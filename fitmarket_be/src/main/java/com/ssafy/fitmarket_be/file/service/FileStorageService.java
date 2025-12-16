package com.ssafy.fitmarket_be.file.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path uploadPath;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        initializeUploadDirectory();
    }

    /**
     * 업로드 디렉토리 초기화.
     */
    private void initializeUploadDirectory() {
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("업로드 디렉토리 생성: {}", uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("업로드 디렉토리를 생성할 수 없습니다: " + uploadPath, e);
        }
    }

    /**
     * 파일 저장 및 URL 반환.
     */
    public String store(MultipartFile file) {
        // 1. 파일 검증
        validateFile(file);

        // 2. 파일명 생성 (UUID + 원본 파일명)
        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + "_" + originalFilename;

        // 3. 파일 저장
        try {
            Path targetPath = uploadPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("파일 저장 완료: {}", storedFilename);

            // 4. URL 반환 (/uploads/파일명)
            return "/uploads/" + storedFilename;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다: " + originalFilename, e);
        }
    }

    /**
     * 파일 검증.
     */
    private void validateFile(MultipartFile file) {
        // 파일 존재 여부
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 5MB 이하여야 합니다. (현재: " + file.getSize() / (1024 * 1024) + "MB)");
        }

        // 파일 확장자 검증
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("파일명이 올바르지 않습니다.");
        }

        String extension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (허용: jpg, jpeg, png, gif)");
        }
    }

    /**
     * 파일 확장자 추출.
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}