package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(name = "UpdateUserProfileRequest", description = "Request body for updating user profile.")
public record UpdateUserProfileRequest(
        @Schema(description = "Display name of the user.", example = "StrangeUser")
        String displayName,
        @Schema(description = "Biography of the user.")
        String bio,
        @Schema(description = "Institution affiliation of the user.")
        String institution,
        @Schema(description = "Research interests of the user.")
        String researchInterests,
        @Schema(description = "Whether the profile is public or not.")
        Boolean profilePublic,
        @Schema(description = "Roles of the user.", examples = {"ROLE_ADMIN", "ROLE_USER"})
        Set<String> academyAffiliations
) {
}
