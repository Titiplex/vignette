package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Scenario storyboard settings update payload")
public record UpdateScenarioStoryboardRequest(
        @Schema(description = "Storyboard layout mode", example = "PRESET")
        String layoutMode,
        @Schema(description = "Storyboard preset key", example = "GRID_3")
        String preset,
        @Schema(description = "Storyboard column count", example = "3")
        Integer columns
) {
}