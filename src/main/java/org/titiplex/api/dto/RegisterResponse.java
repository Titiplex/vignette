package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RegisterResponse", description = "Response after user registration.")
public record RegisterResponse(
        @Schema(description = "User ID (unique).")
        Long id,
        @Schema(description = "Username of the registered user.")
        String username) {
}
