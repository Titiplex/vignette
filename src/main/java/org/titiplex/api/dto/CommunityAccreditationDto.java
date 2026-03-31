package org.titiplex.api.dto;

import org.titiplex.persistence.model.AccreditationPermissionType;
import org.titiplex.persistence.model.AccreditationScopeType;

import java.time.Instant;

public record CommunityAccreditationDto(
        Long id,
        Long userId,
        String username,
        AccreditationPermissionType permissionType,
        AccreditationScopeType scopeType,
        String targetId,
        Long grantedByUserId,
        Instant grantedAt,
        String note
) {
}