package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LanguageOption", description = "Language option for selection, contains minimal information.")
public record LanguageOptionDto(
        @Schema(description = "Language ID", example = "1234ung")
        String id,
        @Schema(description = "Language name", example = "English")
        String name) {
}
