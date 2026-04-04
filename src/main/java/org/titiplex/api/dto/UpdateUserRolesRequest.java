package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(name = "UpdateUserRolesRequest", description = "Payload to replace a user's granted roles.")
public record UpdateUserRolesRequest(
        @Schema(description = "Role names to assign to the user.")
        Set<String> roles
) {
}