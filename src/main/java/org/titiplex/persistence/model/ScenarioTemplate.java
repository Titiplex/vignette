package org.titiplex.persistence.model;

import java.time.Instant;

public record ScenarioTemplate(
        Long id,
        Long sourceScenarioId,
        String title,
        String description,
        String languageId,
        String sourceAuthor,
        Instant createdAt
) {
}
