package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Thumbnail row information with basic metadata")
public record ThumbnailRowDto(
        @Schema(description = "Unique thumbnail identifier")
        Long id,

        @Schema(description = "Title of the thumbnail", example = "Introduction Scene")
        String title,

        @Schema(description = "Index/order of the thumbnail in the scenario", example = "1")
        Integer idx
) {
}
