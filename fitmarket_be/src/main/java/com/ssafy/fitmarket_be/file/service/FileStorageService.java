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
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path uploadPath;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * 허용된 이미지 포맷의 Magic Bytes (파일 시그니처).
     * Key: 확장자, Value: 파일 시작 바이트 배열
     */
    private static final Map<String, byte[][]> MAGIC_BYTES = Map.of(
        "jpg",  new byte[][]{ {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF} },
        "jpeg", new byte[][]{ {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF} },
        "png",  new byte[][]{ {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A} },
        "gif",  new byte[][]{ {0x47, 0x49, 0x46, 0x38, 0x37, 0x61}, {0x47, 0x49, 0x46, 0x38, 0x39, 0x61} }
    );

    private static final int MAX_MAGIC_BYTES_LENGTH = 8;  // PNG가 8바이트로 최대

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

        // 2. 파일명 안전화: 경로 구분자 제거 후 UUID.확장자 형태로만 구성
        String originalFilename = file.getOriginalFilename();
        String safeOriginalFilename = Paths.get(originalFilename).getFileName().toString();
        String extension = getFileExtension(safeOriginalFilename);
        String storedFilename = UUID.randomUUID() + "." + extension;

        // 3. 파일 저장
        try {
            Path targetPath = uploadPath.resolve(storedFilename).normalize();

            // 4. 최종 경로가 업로드 디렉터리 하위인지 검증
            if (!targetPath.startsWith(uploadPath)) {
                throw new IllegalArgumentException("잘못된 파일 경로입니다.");
            }

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("파일 저장 완료: {}", storedFilename);

            // 5. URL 반환 (/uploads/파일명)
            return "/uploads/" + storedFilename;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
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

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (허용: jpg, jpeg, png, gif)");
        }

        // Magic Bytes 검증 (파일 헤더가 실제 이미지인지 확인)
        validateMagicBytes(file, extension);
    }

    /**
     * 파일의 첫 N바이트를 읽어 실제 파일 타입이 확장자와 일치하는지 검증한다.
     *
     * @param file      업로드된 파일
     * @param extension 파일 확장자 (소문자)
     */
    private void validateMagicBytes(MultipartFile file, String extension) {
        byte[][] expectedSignatures = MAGIC_BYTES.get(extension);
        if (expectedSignatures == null) {
            throw new IllegalArgumentException("파일 형식 검증을 지원하지 않습니다: " + extension);
        }

        try {
            byte[] fileHeader = new byte[MAX_MAGIC_BYTES_LENGTH];
            int bytesRead = file.getInputStream().read(fileHeader);
            if (bytesRead < 0) {
                throw new IllegalArgumentException("파일 내용을 읽을 수 없습니다.");
            }

            boolean matched = false;
            for (byte[] signature : expectedSignatures) {
                if (bytesRead >= signature.length && startsWith(fileHeader, signature)) {
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                throw new IllegalArgumentException(
                    "파일 내용이 확장자(" + extension + ")와 일치하지 않습니다. 실제 이미지 파일을 업로드해 주세요."
                );
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("파일 검증 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 바이트 배열이 지정된 접두사로 시작하는지 확인한다.
     */
    private boolean startsWith(byte[] data, byte[] prefix) {
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
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