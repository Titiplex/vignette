package org.titiplex.api.security;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("isAuthenticated()")
@SecurityRequirement(name = "bearerAuth")
@ApiAccess(level = ApiAccessLevel.AUTHENTICATED, rule = "Requires authentication.")
public @interface AuthenticatedOperation {
}