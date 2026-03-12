package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(name = "UserProfileResponse", description = "Response containing user profile information.")
public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String displayName,
        String bio,
        String institution,
        String researchInterests,
        boolean profilePublic,
        Set<String> roles,
        Set<String> academyAffiliations
) {
}
