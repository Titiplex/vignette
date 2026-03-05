package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RegisterRequest", description = "Request body for user registration.")
public record RegisterRequest(
        @Schema(description = "Username of the new user.")
        String username,
        @Schema(description = "Email address of the new user.", example = "name@address.domain")
        String email,
        @Schema(description = "Password of the new user.")
        String password
) {}