package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "Scenario", description = "Scenario information")
public record ScenarioDto(
        @Schema(description = "Scenario ID (unique).")
        Long id,
        @Schema(description = "Scenario title.", example = "My scenario")
        String title,
        @Schema(description = "Scenario description.", example = "This is a scenario.")
        String description,
        @Schema(description = "ID of the language used in the scenario.", example = "1234ung")
        String languageId,
        @Schema(description = "Username of the author of the scenario.")
        String authorUsername,
        @Schema(description = "Creation date of the scenario.")
        Instant createdAt
) {
}