package org.titiplex.api.dto;

import java.time.Instant;

public record ScenarioTemplateDto(
        Long id,
        Long sourceScenarioId,
        String title,
        String description,
        String languageId,
        String sourceAuthor,
        Instant createdAt
) {
}
