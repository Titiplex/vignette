package org.titiplex.api.dto;

import java.util.Set;

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
