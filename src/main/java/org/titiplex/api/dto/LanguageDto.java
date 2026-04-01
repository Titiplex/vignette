package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Language", description = "Language information")
public record LanguageDto(
        @Schema(description = "Language ID", example = "1234ung")
        String id,

        @Schema(description = "Language name", example = "English")
        String name,

        @Schema(description = "Language level", examples = {"Dialect", "Language", "Family"})
        String level,

        @Schema(description = "Is book-kept. Indicates if a particular classification isn't accepted anymore because of reanalysing its classification.")
        Boolean bookkeeping,

        @Schema(description = "ISO 639 code")
        String iso639P3code,

        @Schema(description = "Latitude")
        Float latitude,

        @Schema(description = "Longitude")
        Float longitude,

        @Schema(description = "Country in which the language is/was present IDs")
        String countryIds,

        @Schema(description = "Description of the language")
        String description,

        @Schema(description = "Markup description of the language")
        String markupDescription,

        @Schema(description = "Description of the family to which the language belongs.")
        String familyId,

        @Schema(description = "Name of the family to which the language belongs.")
        String familyName,

        @Schema(description = "ID of the parent language/group/family.")
        String parentId,

        @Schema(description = "Name of the parent language/group/family.")
        String parentName
) {
}