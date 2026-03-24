package org.titiplex.api.dto;

import org.titiplex.persistence.model.AccreditationScopeType;

public record GrantAccreditationBody(Long userId,
                                     AccreditationScopeType scopeType,
                                     Long scenarioId,
                                     String note) {
}
