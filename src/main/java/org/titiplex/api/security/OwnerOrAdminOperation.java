package org.titiplex.api.security;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OwnerOrAdminOperation {

    ProtectedResource resource();

    /**
     * Name of the method parameter that contains the protected resource id.
     * Example: "id", "scenarioId", "thumbnailId", "audioId"
     */
    String param() default "id";

    /**
     * Optional custom access description for documentation.
     * If blank, a default one is generated.
     */
    String description() default "";
}