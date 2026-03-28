package org.titiplex.config.components;

import org.titiplex.api.security.ProtectedResource;

public interface OwnershipSecurityService {
    boolean isOwner(ProtectedResource resource, Object resourceId, String username);
}