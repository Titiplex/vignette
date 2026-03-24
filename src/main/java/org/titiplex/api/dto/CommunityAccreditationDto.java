package org.titiplex.api.dto;

import org.titiplex.persistence.model.AccreditationScopeType;

import java.time.Instant;

public record CommunityAccreditationDto(Long id,
                                        Long userId,
                                        String username,
                                        AccreditationScopeType scopeType,
                                        Long scenarioId,
                                        Long grantedByUserId,
                                        Instant grantedAt,
                                        String note) {
}