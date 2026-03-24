package org.titiplex.api.dto;

import org.titiplex.persistence.model.AccreditationRequestStatus;
import org.titiplex.persistence.model.AccreditationScopeType;

import java.time.Instant;

public record AccreditationRequestDto(Long id,
                                      Long requestedByUserId,
                                      String requestedByUsername,
                                      AccreditationScopeType scopeType,
                                      Long scenarioId,
                                      String motivation,
                                      AccreditationRequestStatus status,
                                      Long reviewedByUserId,
                                      String reviewNote,
                                      Instant createdAt,
                                      Instant reviewedAt) {
}
