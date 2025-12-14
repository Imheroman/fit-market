package com.ssafy.fitmarket_be.file.controller;

import com.ssafy.fitmarket_be.file.dto.FileUploadResponse;
import com.ssafy.fitmarket_be.file.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    /**
     * 파일 업로드.
     *
     * @param file 업로드할 파일
     * @return 업로드된 파일의 URL
     */
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
        @RequestParam("file") MultipartFile file
    ) {
        log.info("파일 업로드 요청: {}, 크기: {}bytes", file.getOriginalFilename(), file.getSize());

        try {
            String fileUrl = fileStorageService.store(file);
            log.info("파일 업로드 성공: {}", fileUrl);

            return ResponseEntity.ok(new FileUploadResponse(fileUrl));
        } catch (IllegalArgumentException e) {
            log.warn("파일 업로드 실패 (유효성 검증): {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("파일 업로드 실패: {}", e.getMessage(), e);
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }
}