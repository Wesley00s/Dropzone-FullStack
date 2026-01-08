package com.dropzone.api.v1.controller;

import com.dropzone.api.v1.dto.ApiResponse;
import com.dropzone.api.v1.dto.FileResponseDTO;
import com.dropzone.api.v1.dto.PaginationResponse;
import com.dropzone.core.services.FileService;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Data
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<FileResponseDTO>> upload(
            @RequestParam("files") List<MultipartFile> files
    ) {
        List<FileResponseDTO> response = fileService.uploadAll(files);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Map<String, String>> download(@PathVariable UUID id) {
        String fileUrl = fileService.generateDownloadUrl(id);

        return ResponseEntity.ok(Map.of("url", fileUrl));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<FileResponseDTO>> listAll(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        var filePage = fileService.listAll(page, size);
        var totalSize = fileService.getTotalSize();
        var response = new ApiResponse<>(
                filePage.getContent(),
                new PaginationResponse(
                        filePage.getNumber(),
                        filePage.getSize(),
                        filePage.getTotalElements(),
                        filePage.getTotalPages(),
                        totalSize
                )
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        fileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}