package org.titiplex.api.security;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SecurityRequirement(name = "bearerAuth")
@ApiAccess(level = ApiAccessLevel.PUBLIC, rule = "Public Endpoint, no authentication required.")
public @interface PublicOperation {
}
