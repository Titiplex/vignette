package org.titiplex.api.dto;

import java.util.Set;

public record MeResponse(Long id, String username, String email, Set<String> roles) {
}
