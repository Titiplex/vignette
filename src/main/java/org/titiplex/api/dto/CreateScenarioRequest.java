package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "CreateScenarioRequest", description = "Request body for creating a new scenario.")
public record CreateScenarioRequest(
        @Schema(description = "Title of the new scenario.", example = "My new scenario")
        String title,
        @Schema(description = "Description of the new scenario.", example = "This is a new scenario.")
        String description,
        @Schema(description = "ID of the language to use for the scenario.", example = "1234ung")
        String languageId,
        @Schema(description = "Tags to associate with the scenario.")
        List<String> tags
) {
}
