package com.dropzone.domain.repo;

import com.dropzone.domain.model.FileMetadata;
import com.dropzone.infra.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FileMetadataRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private FileMetadataRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Should save and find FileMetadata by ID in Real Postgres")
    void shouldSaveAndFindById() {
        FileMetadata file = FileMetadata.builder()
                .originalName("test-integration.pdf")
                .contentType("application/pdf")
                .size(2048L)
                .storageKey("s3-key-real-123")
                .createdAt(Instant.now())
                .build();

        FileMetadata savedFile = repository.save(file);

        Optional<FileMetadata> foundFile = repository.findById(savedFile.getId());

        assertThat(foundFile).isPresent();
        assertThat(foundFile.get().getId()).isEqualTo(savedFile.getId());
        assertThat(foundFile.get().getOriginalName()).isEqualTo("test-integration.pdf");
        assertThat(foundFile.get().getStorageKey()).isEqualTo("s3-key-real-123");
    }

    @Test
    @DisplayName("Should delete FileMetadata in Real Postgres")
    void shouldDeleteFileMetadata() {
        FileMetadata file = FileMetadata.builder()
                .originalName("delete.txt")
                .contentType("text/plain")
                .size(10L)
                .storageKey("key-delete")
                .createdAt(Instant.now())
                .build();

        FileMetadata savedFile = repository.save(file);

        repository.deleteById(savedFile.getId());

        Optional<FileMetadata> deletedFile = repository.findById(savedFile.getId());
        assertThat(deletedFile).isEmpty();
    }
}