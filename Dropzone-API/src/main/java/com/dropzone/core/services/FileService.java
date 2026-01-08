package com.dropzone.core.services;

import com.dropzone.api.v1.dto.FileResponseDTO;
import com.dropzone.domain.model.FileMetadata;
import com.dropzone.domain.repo.FileMetadataRepository;
import com.dropzone.infra.provider.StorageProvider;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Data
public class FileService {

    private final StorageProvider storageProvider;
    private final FileMetadataRepository repository;

    @Transactional
    public List<FileResponseDTO> uploadAll(List<MultipartFile> files) {
        return files.stream()
                .map(this::upload)
                .collect(Collectors.toList());
    }

    @Transactional
    public FileResponseDTO upload(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("Empty file is not allowed");

        String storageKey = UUID.randomUUID() + "-" + file.getOriginalFilename();

        try {
            storageProvider.save(storageKey, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (IOException e) {
            throw new RuntimeException("Error while reading file", e);
        }

        FileMetadata metadata = FileMetadata.builder()
                .originalName(file.getOriginalFilename())
                .storageKey(storageKey)
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();

        repository.save(metadata);

        String url = storageProvider.generateUrl(storageKey, metadata.getOriginalName());

        return new FileResponseDTO(
                metadata.getId(),
                metadata.getOriginalName(),
                metadata.getContentType(),
                metadata.getSize(),
                url,
                metadata.getCreatedAt()
        );
    }

    public String generateDownloadUrl(UUID id) {
        var fileMetadata = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        return storageProvider.generateUrl(
                fileMetadata.getStorageKey(),
                fileMetadata.getOriginalName()
        );
    }

    public Page<FileResponseDTO> listAll(Integer page, Integer size) {
        return repository.findAll
                        (PageRequest.of(page, size))
                .map(metadata -> new FileResponseDTO(
                        metadata.getId(),
                        metadata.getOriginalName(),
                        metadata.getContentType(),
                        metadata.getSize(),
                        storageProvider.generateUrl(metadata.getStorageKey(), metadata.getOriginalName()),
                        metadata.getCreatedAt()
                ));
    }

    @Transactional
    public void delete(UUID id) {
        var fileMetadata = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));
        storageProvider.delete(fileMetadata.getStorageKey());
        repository.delete(fileMetadata);
    }

    public Long getTotalSize() {
        return repository.getTotalStorageUsage();
    }
}