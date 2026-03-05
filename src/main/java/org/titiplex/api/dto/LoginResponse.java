package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginResponse", description = "Response after user login.")
public record LoginResponse(
        @Schema(description = "JWT access token.")
        String accessToken,
        @Schema(description = "JWT expiration time in seconds.")
        long expiresInSeconds) {
}
