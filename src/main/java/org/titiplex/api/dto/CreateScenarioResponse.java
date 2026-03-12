package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateScenarioResponse", description = "Response for creating a new scenario")
public record CreateScenarioResponse(
        @Schema(description = "ID of the created scenario", example = "42")
        Long id
) {
}
