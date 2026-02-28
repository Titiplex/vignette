package org.titiplex.api.dto;

import java.util.Set;

public record UpdateUserProfileRequest(
        String displayName,
        String bio,
        String institution,
        String researchInterests,
        Boolean profilePublic,
        Set<String> academyAffiliations
) {
}
