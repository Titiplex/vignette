package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AdminOverview", description = "Global administration overview counters.")
public record AdminOverviewDto(
        @Schema(description = "Total number of users.", example = "12")
        long userCount,

        @Schema(description = "Total number of scenarios.", example = "37")
        long scenarioCount,

        @Schema(description = "Number of published scenarios.", example = "18")
        long publishedScenarioCount,

        @Schema(description = "Number of draft/private scenarios.", example = "19")
        long draftScenarioCount
) {
}