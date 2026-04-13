package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ScenarioTagSuggestion", description = "Scenario tag suggestion (fetching or creating a tag).")
public record ScenarioTagSuggestionDto(
        @Schema(description = "Tag ID (unique).", example = "1")
        Long id,
        @Schema(description = "Tag name.", example = "Phonology")
        String name,
        @Schema(description = "Normalized tag name.", example = "phonology")
        String normalizedName
) {
}