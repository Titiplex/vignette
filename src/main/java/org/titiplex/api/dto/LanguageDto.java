package org.titiplex.api.dto;

public record LanguageDto(
        String id,
        String name,
        String level,
        Boolean bookkeeping,
        String iso639P3code,
        Float latitude,
        Float longitude,
        String countryIds,
        String familyId,
        String familyName,
        String parentId,
        String parentName
) {}