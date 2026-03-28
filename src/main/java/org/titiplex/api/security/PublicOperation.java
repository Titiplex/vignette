package org.titiplex.api.security;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ApiAccess(level = ApiAccessLevel.PUBLIC, rule = "Public Endpoint, no authentication required.")
public @interface PublicOperation {
}
