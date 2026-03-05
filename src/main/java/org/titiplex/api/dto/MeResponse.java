package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "MeResponse", description = "Response containing user self information.")
public record MeResponse(
        @Schema(description = "User ID (unique).")
        Long id,
        @Schema(description = "Username of the user.")
        String username,
        @Schema(description = "Roles of the user.", examples = {
                "ROLE_USER",
                "ROLE_ADMIN"
        })
        List<String> roles) {
}
