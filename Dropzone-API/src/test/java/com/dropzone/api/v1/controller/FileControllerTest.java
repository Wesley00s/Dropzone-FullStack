package com.dropzone.api.v1.controller;

import com.dropzone.api.v1.dto.FileResponseDTO;
import com.dropzone.core.services.FileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;

    @Test
    @DisplayName("POST /api/files - Should return 201 Created on upload")
    void shouldReturnCreatedOnUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "doc.pdf", "application/pdf", "dados".getBytes()
        );

        FileResponseDTO responseMock = new FileResponseDTO(
                UUID.randomUUID(), "doc.pdf", "application/pdf", 100L, "http://url", Instant.now()
        );

        when(fileService.uploadAll(any())).thenReturn(List.of(responseMock));

        mockMvc.perform(multipart("/api/files").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].originalName").value("doc.pdf"))
                .andExpect(jsonPath("$[0].downloadUrl").value("http://url"));
    }

    @Test
    @DisplayName("GET /api/files/{id}/download - Should return URL JSON")
    void shouldReturnUrlOnDownload() throws Exception {
        UUID id = UUID.randomUUID();
        String fakeUrl = "http://localhost:4566/bucket/file.pdf?token=123";

        when(fileService.generateDownloadUrl(id)).thenReturn(fakeUrl);

        mockMvc.perform(get("/api/files/{id}/download", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(fakeUrl));
    }

    @Test
    @DisplayName("GET /api/files - Should return paginated list of files")
    void shouldReturnPaginatedList() throws Exception {
        int page = 0;
        int size = 10;

        FileResponseDTO dto = new FileResponseDTO(
                UUID.randomUUID(),
                "report.pdf",
                "application/pdf",
                2048L,
                "http://s3.url/report.pdf",
                Instant.now()
        );

        var pageResponse = new PageImpl<>(List.of(dto), PageRequest.of(page, size), 1);

        when(fileService.listAll(page, size)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/files")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].originalName").value("report.pdf"))
                .andExpect(jsonPath("$.pagination.totalElements").value(1));
    }

    @Test
    @DisplayName("DELETE /api/files/{id} - Should return 204 No Content on delete")
    void shouldReturnNoContentOnDelete() throws Exception {
        UUID id = UUID.randomUUID();

        doNothing().when(fileService).delete(id);

        mockMvc.perform(delete("/api/files/{id}", id))
                .andExpect(status().isNoContent());
    }
}