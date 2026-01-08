package com.dropzone.api.v1.dto;

public record PaginationResponse(
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages,
        Long totalSize
) {
}