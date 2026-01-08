package com.dropzone.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

public record FileResponseDTO(
    UUID id,
    String originalName,
    String contentType,
    Long size,
    String downloadUrl,
    Instant uploadedAt
) {}