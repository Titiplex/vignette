package org.titiplex.api.security;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
@ApiAccess(level = ApiAccessLevel.USER, rule = "Requires authentication. Required role: USER.")
public @interface UserOperation {
}