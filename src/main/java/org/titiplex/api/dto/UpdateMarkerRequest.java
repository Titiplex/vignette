package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateMarkerRequest", description = "Request body for updating marker position.")
public record UpdateMarkerRequest(
        @Schema(
                description = "the x-coordinate of the marker (null if not set)",
                minimum = "0",
                maximum = "100",
                defaultValue = "null"
        )
        Double markerX,
        @Schema(
                description = "the y-coordinate of the marker (null if not set)",
                minimum = "0",
                maximum = "100",
                defaultValue = "null"
        )
        Double markerY,
        @Schema(
                description = "the label for the marker (can be null or blank if not set)",
                defaultValue = "\"\""
        )
        String markerLabel) {
}
