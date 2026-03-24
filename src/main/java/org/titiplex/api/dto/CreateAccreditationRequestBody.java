package org.titiplex.api.dto;

import org.titiplex.persistence.model.AccreditationScopeType;

public record CreateAccreditationRequestBody(AccreditationScopeType scopeType,
                                             Long scenarioId,
                                             String motivation) {
}
