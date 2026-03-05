package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after successful thumbnail upload")
public record UploadResponse(
        @Schema(description = "ID of the uploaded thumbnail", example = "42")
        Long id
) {
}
