package org.titiplex.api.security;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiAccess {
    ApiAccessLevel level();

    String rule() default "";

    String ownerResource() default "";
}