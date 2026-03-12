package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginRequest", description = "Request body for user login.")
public record LoginRequest(
        @Schema(description = "Username of the user.")
        String username,
        @Schema(description = "Password of the user.")
        String password) {
}
