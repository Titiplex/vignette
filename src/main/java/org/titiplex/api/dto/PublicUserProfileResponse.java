package org.titiplex.api.dto;

import java.util.Set;

public record PublicUserProfileResponse(
        Long id,
        String username,
        String displayName,
        String bio,
        String institution,
        String researchInterests,
        Set<String> roles,
        Set<String> academyAffiliations
) {
}
