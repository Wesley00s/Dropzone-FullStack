package com.dropzone.core.services;

import com.dropzone.api.v1.dto.FileResponseDTO;
import com.dropzone.domain.model.FileMetadata;
import com.dropzone.domain.repo.FileMetadataRepository;
import com.dropzone.infra.provider.StorageProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private StorageProvider storageProvider;

    @Mock
    private FileMetadataRepository repository;

    @InjectMocks
    private FileService fileService;

    @Test
    @DisplayName("Should upload multiple files successfully")
    void shouldUploadMultipleFilesSuccessfully() {
        MockMultipartFile file1 = new MockMultipartFile(
                "files", "doc1.pdf", "application/pdf", "content1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "files", "doc2.jpg", "image/jpeg", "content2".getBytes()
        );
        List<MultipartFile> files = List.of(file1, file2);

        when(storageProvider.save(anyString(), any(InputStream.class), anyLong(), anyString()))
                .thenReturn("storage-key-mock");

        when(storageProvider.generateUrl(anyString(), anyString()))
                .thenReturn("http://localhost/signed-url");

        when(repository.save(any(FileMetadata.class))).thenAnswer(invocation -> {
            FileMetadata f = invocation.getArgument(0);
            f.setId(UUID.randomUUID());
            return f;
        });

        List<FileResponseDTO> response = fileService.uploadAll(files);

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("doc1.pdf", response.get(0).originalName());
        assertEquals("doc2.jpg", response.get(1).originalName());

        verify(storageProvider, times(2)).save(anyString(), any(), anyLong(), anyString());
        verify(repository, times(2)).save(any(FileMetadata.class));
    }

    @Test
    @DisplayName("Should upload file successfully")
    void shouldUploadFileSuccessfully() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes()
        );

        when(storageProvider.save(anyString(), any(InputStream.class), anyLong(), anyString()))
                .thenReturn("uuid-test.txt");

        when(storageProvider.generateUrl(anyString(), anyString())).thenReturn("http://localhost/signed-url");

        when(repository.save(any(FileMetadata.class))).thenAnswer(invocation -> {
            FileMetadata f = invocation.getArgument(0);
            f.setId(UUID.randomUUID());
            f.setCreatedAt(Instant.now());
            return f;
        });

        FileResponseDTO response = fileService.upload(file);

        assertNotNull(response.id());
        assertEquals("test.txt", response.originalName());
        assertEquals("http://localhost/signed-url", response.downloadUrl());

        verify(storageProvider, times(1)).save(anyString(), any(), anyLong(), anyString());
        verify(repository, times(1)).save(any(FileMetadata.class));
    }

    @Test
    @DisplayName("Should throw exception when file is empty")
    void shouldThrowExceptionWhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "", "text/plain", new byte[0]
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> fileService.upload(emptyFile));

        assertEquals("Empty file is not allowed", exception.getMessage());
        verifyNoInteractions(storageProvider);
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Should throw RuntimeException when IOException occurs during upload")
    void shouldThrowRuntimeExceptionOnIOException() throws IOException {
        MultipartFile fileMock = mock(MultipartFile.class);

        when(fileMock.isEmpty()).thenReturn(false);
        when(fileMock.getOriginalFilename()).thenReturn("error.txt");
        when(fileMock.getInputStream()).thenThrow(new IOException("Disk error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> fileService.upload(fileMock));

        assertEquals("Error while reading file", exception.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Should generate download URL successfully")
    void shouldGenerateDownloadUrlSuccessfully() {
        UUID id = UUID.randomUUID();
        FileMetadata metadata = new FileMetadata();
        metadata.setId(id);
        metadata.setStorageKey("s3-key-123");
        metadata.setOriginalName("arquivo.txt");
        when(repository.findById(id)).thenReturn(Optional.of(metadata));

        when(storageProvider.generateUrl("s3-key-123", "arquivo.txt")).thenReturn("http://signed.url");

        String url = fileService.generateDownloadUrl(id);

        assertEquals("http://signed.url", url);
    }

    @Test
    @DisplayName("Should throw exception when generating URL for non-existent file")
    void shouldThrowExceptionWhenGeneratingUrlForNonExistentFile() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> fileService.generateDownloadUrl(id));

        assertEquals("File not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should list all files with pagination")
    void shouldListAllFilesWithPagination() {
        int page = 0;
        int size = 10;
        FileMetadata metadata = new FileMetadata();
        metadata.setId(UUID.randomUUID());
        metadata.setOriginalName("doc.pdf");
        metadata.setStorageKey("key-doc");
        metadata.setSize(1024L);
        metadata.setCreatedAt(Instant.now());

        Page<FileMetadata> pageData = new PageImpl<>(List.of(metadata));

        when(repository.findAll(any(Pageable.class))).thenReturn(pageData);

        when(storageProvider.generateUrl("key-doc", "doc.pdf")).thenReturn("http://url.com");

        Page<FileResponseDTO> result = fileService.listAll(page, size);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("doc.pdf", result.getContent().getFirst().originalName());
        assertEquals("http://url.com", result.getContent().getFirst().downloadUrl());

        verify(repository).findAll(PageRequest.of(page, size));
    }

    @Test
    @DisplayName("Should delete file successfully")
    void shouldDeleteFileSuccessfully() {
        UUID id = UUID.randomUUID();
        FileMetadata metadata = new FileMetadata();
        metadata.setStorageKey("key-123");

        when(repository.findById(id)).thenReturn(Optional.of(metadata));

        fileService.delete(id);

        verify(storageProvider, times(1)).delete("key-123");
        verify(repository, times(1)).delete(metadata);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent file")
    void shouldThrowExceptionWhenDeletingNonExistentFile() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> fileService.delete(id));

        assertEquals("File not found", exception.getMessage());
        verify(storageProvider, never()).delete(anyString());
        verify(repository, never()).delete(any(FileMetadata.class));
    }

    @Test
    @DisplayName("Should return total storage usage")
    void shouldReturnTotalStorageUsage() {
        Long expectedSize = 5000L;
        when(repository.getTotalStorageUsage()).thenReturn(expectedSize);

        Long result = fileService.getTotalSize();

        assertNotNull(result);
        assertEquals(expectedSize, result);
        verify(repository, times(1)).getTotalStorageUsage();
    }
}