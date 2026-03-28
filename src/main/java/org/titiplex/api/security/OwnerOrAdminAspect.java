package org.titiplex.api.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.titiplex.config.components.OwnershipSecurityService;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@org.aspectj.lang.annotation.Aspect
@Component
public class OwnerOrAdminAspect {

    private final OwnershipSecurityService ownershipSecurityService;

    public OwnerOrAdminAspect(OwnershipSecurityService ownershipSecurityService) {
        this.ownershipSecurityService = ownershipSecurityService;
    }

    @Around("@annotation(ownerOp)")
    public Object checkOwnerOrAdmin(ProceedingJoinPoint pjp, OwnerOrAdminOperation ownerOp) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || isAnonymous(auth)) {
            throw new InsufficientAuthenticationException("Authentication required");
        }

        if (isAdmin(auth)) {
            return pjp.proceed();
        }

        Object resourceId = extractNamedArgument(pjp, ownerOp.param());
        if (resourceId == null) {
            throw new IllegalStateException(
                    "Could not resolve parameter '" + ownerOp.param() + "' for @OwnerOrAdminOperation"
            );
        }

        String username = auth.getName();
        boolean allowed = ownershipSecurityService.isOwner(ownerOp.resource(), resourceId, username);

        if (!allowed) {
            throw new AccessDeniedException("Authenticated but not allowed for this resource");
        }

        return pjp.proceed();
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> {
            String role = a.getAuthority();
            return "ADMIN".equals(role) || "ROLE_ADMIN".equals(role);
        });
    }

    private boolean isAnonymous(Authentication auth) {
        return "anonymousUser".equals(auth.getPrincipal());
    }

    private Object extractNamedArgument(ProceedingJoinPoint pjp, String paramName) {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Method method = sig.getMethod();
        Parameter[] parameters = method.getParameters();
        Object[] args = pjp.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(paramName)) {
                return args[i];
            }
        }
        return null;
    }
}