package org.titiplex.api.dto;

public record UpdateLanguageRequest(
        String name,
        String level,
        Boolean bookkeeping,
        String iso639P3code,
        Float latitude,
        Float longitude,
        String countryIds,
        String familyId,
        String parentId,
        String description,
        String markupDescription
) {
}