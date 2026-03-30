package com.ssafy.fitmarket_be.unit.file;

import com.ssafy.fitmarket_be.file.service.FileStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileStorageService")
class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        // TempDir is cleaned up automatically by JUnit 5
        // Additional cleanup if needed
        File[] files = tempDir.toFile().listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    // ===== store() =====

    @Test
    @DisplayName("store: 정상 jpg 파일 저장 시 /uploads/ 경로 URL을 반환한다")
    void store_정상jpg_URL반환() {
        // given
        byte[] content = new byte[100];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                content
        );

        // when
        String result = fileStorageService.store(file);

        // then
        assertThat(result).startsWith("/uploads/");
        assertThat(result).endsWith(".jpg");
    }

    @Test
    @DisplayName("store: 빈 파일 전달 시 IllegalArgumentException을 던진다")
    void store_빈파일_IllegalArgumentException() {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]  // 0 bytes — isEmpty() == true
        );

        // when & then
        assertThatThrownBy(() -> fileStorageService.store(emptyFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("파일이 비어있습니다.");
    }

    @Test
    @DisplayName("store: 5MB 초과 파일 전달 시 IllegalArgumentException을 던진다")
    void store_5MB초과_IllegalArgumentException() {
        // given
        byte[] oversizedContent = new byte[5 * 1024 * 1024 + 1];  // 5MB + 1 byte
        MockMultipartFile oversizedFile = new MockMultipartFile(
                "file",
                "large-image.jpg",
                "image/jpeg",
                oversizedContent
        );

        // when & then
        assertThatThrownBy(() -> fileStorageService.store(oversizedFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("파일 크기는 5MB 이하여야 합니다.");
    }

    @Test
    @DisplayName("store: 허용되지 않는 확장자(exe) 전달 시 IllegalArgumentException을 던진다")
    void store_허용안되는확장자_IllegalArgumentException() {
        // given
        byte[] content = new byte[100];
        MockMultipartFile exeFile = new MockMultipartFile(
                "file",
                "malware.exe",
                "application/octet-stream",
                content
        );

        // when & then
        assertThatThrownBy(() -> fileStorageService.store(exeFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않는 파일 형식입니다.");
    }

    @Test
    @DisplayName("store: 경로 탐색 공격(../../../etc/passwd) 시 IllegalArgumentException을 던진다")
    void store_경로탐색공격_IllegalArgumentException() {
        // given
        // The service strips path components via Paths.get().getFileName(),
        // so "../../../etc/passwd" becomes "passwd" — no extension → not in allowed list.
        byte[] content = new byte[100];
        MockMultipartFile traversalFile = new MockMultipartFile(
                "file",
                "../../../etc/passwd",
                "application/octet-stream",
                content
        );

        // when & then
        assertThatThrownBy(() -> fileStorageService.store(traversalFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않는 파일 형식입니다.");
    }

    @Test
    @DisplayName("store: 확장자 없는 파일명 전달 시 IllegalArgumentException을 던진다")
    void store_확장자없는파일명_IllegalArgumentException() {
        // given
        byte[] content = new byte[100];
        MockMultipartFile noExtFile = new MockMultipartFile(
                "file",
                "noextension",
                "application/octet-stream",
                content
        );

        // when & then
        assertThatThrownBy(() -> fileStorageService.store(noExtFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("허용되지 않는 파일 형식입니다.");
    }
}
