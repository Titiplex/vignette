package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Standard API error response")
public record ApiError(
        @Schema(example = "2026-03-18T14:00:00Z")
        String timestamp,

        @Schema(example = "400")
        int status,

        @Schema(example = "Bad Request")
        String error,

        @Schema(example = "username required")
        String message,

        @Schema(example = "/api/auth/register")
        String path
) {
}