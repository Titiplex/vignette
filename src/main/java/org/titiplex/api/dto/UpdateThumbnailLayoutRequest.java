package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thumbnail storyboard placement update payload")
public record UpdateThumbnailLayoutRequest(
        @Schema(description = "Grid column start")
        Integer gridColumn,
        @Schema(description = "Grid row start")
        Integer gridRow,
        @Schema(description = "Grid column span")
        Integer gridColumnSpan,
        @Schema(description = "Grid row span")
        Integer gridRowSpan
) {
}