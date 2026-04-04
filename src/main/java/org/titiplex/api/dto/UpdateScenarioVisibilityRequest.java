package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateScenarioVisibilityRequest", description = "Payload to update scenario visibility.")
public record UpdateScenarioVisibilityRequest(
        @Schema(description = "Target visibility status.", example = "PUBLISHED")
        String visibilityStatus
) {
}