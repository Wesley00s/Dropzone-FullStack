package com.dropzone.api.v1.dto;

import java.util.List;

public record ApiResponse<T>(
        List<T> data,
        PaginationResponse pagination
) {
}