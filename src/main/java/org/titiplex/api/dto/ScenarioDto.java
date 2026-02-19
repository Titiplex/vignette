package org.titiplex.api.dto;

import java.time.Instant;

public record ScenarioDto(
        Long id,
        String title,
        String description,
        String languageId,
        String authorUsername,
        Instant createdAt
) {
}