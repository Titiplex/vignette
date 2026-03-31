package org.titiplex.api.dto;

import org.titiplex.persistence.model.AccreditationPermissionType;
import org.titiplex.persistence.model.AccreditationRequestStatus;
import org.titiplex.persistence.model.AccreditationScopeType;

import java.time.Instant;

public record AccreditationRequestDto(
        Long id,
        Long requestedByUserId,
        String requestedByUsername,
        AccreditationPermissionType permissionType,
        AccreditationScopeType scopeType,
        String targetId,
        String motivation,
        AccreditationRequestStatus status,
        Long reviewedByUserId,
        String reviewNote,
        Instant createdAt,
        Instant reviewedAt
) {
}