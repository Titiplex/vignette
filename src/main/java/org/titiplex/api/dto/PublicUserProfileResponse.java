package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(name = "PublicUserProfileResponse", description = "Response containing public user profile information.")
public record PublicUserProfileResponse(
        @Schema(description = "User ID (unique).")
        Long id,
        @Schema(description = "Username of the user.")
        String username,
        @Schema(description = "Display name of the user, can be different from the username and doesn't serve as id.")
        String displayName,
        @Schema(description = "Self-written biography of the user.", example = "I obtained my masters' degree in Université de Montréal")
        String bio,
        @Schema(description = "Institutions where the user is affiliated.", example = "University of Montréal, MIT, Oxford University")
        String institution,
        @Schema(description = "Research interests of the user.", example = "Diachronical phonology in Mayan languages.")
        String researchInterests,
        @Schema(description = "Roles of the user.", examples = {"ROLE_ADMIN", "ROLE_USER"})
        Set<String> roles,
        @Schema(description = "Academy affiliations of the user.", examples = {
                "Member of Team Chuj",
                "Writer at revue of Languages"
        })
        Set<String> academyAffiliations
) {
}
