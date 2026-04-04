package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(name = "AdminUserRow", description = "User row for administration listing.")
public record AdminUserRowDto(
        @Schema(description = "User ID.", example = "5")
        Long id,

        @Schema(description = "Username.", example = "alice")
        String username,

        @Schema(description = "Email.", example = "alice@example.com")
        String email,

        @Schema(description = "Display name.", example = "Alice Martin")
        String displayName,

        @Schema(description = "Granted roles.")
        Set<String> roles,

        @Schema(description = "Public profile flag.", example = "true")
        boolean profilePublic
) {
}