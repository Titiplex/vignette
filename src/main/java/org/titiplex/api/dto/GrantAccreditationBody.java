package org.titiplex.api.dto;

import org.titiplex.persistence.model.AccreditationPermissionType;
import org.titiplex.persistence.model.AccreditationScopeType;

public record GrantAccreditationBody(
        Long userId,
        AccreditationPermissionType permissionType,
        AccreditationScopeType scopeType,
        String targetId,
        String note
) {
}