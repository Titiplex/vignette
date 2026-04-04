package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateScenarioMetadataRequest", description = "Editable metadata for a scenario.")
public record UpdateScenarioMetadataRequest(
        @Schema(description = "Scenario title.", example = "My revised scenario")
        String title,

        @Schema(description = "Scenario description.", example = "Updated description for the scenario.")
        String description
) {
}