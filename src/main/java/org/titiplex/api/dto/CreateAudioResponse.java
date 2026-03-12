package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateAudioResponse", description = "Response after successful audio file creation.")
public record CreateAudioResponse(
        @Schema(description = "ID of the created audio file", example = "42")
        Long id
) {
}