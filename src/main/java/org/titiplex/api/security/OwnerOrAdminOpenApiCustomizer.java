package org.titiplex.api.security;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OwnerOrAdminOpenApiCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        OwnerOrAdminOperation ann =
                AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), OwnerOrAdminOperation.class);

        if (ann == null) {
            return operation;
        }

        ensureBearerSecurity(operation);
        enrichDescription(operation, ann);
        ensure401(operation);
        ensure403(operation);
        addExtensions(operation, ann);

        return operation;
    }

    private void ensureBearerSecurity(Operation operation) {
        if (operation.getSecurity() == null || operation.getSecurity().isEmpty()) {
            operation.setSecurity(List.of(new SecurityRequirement().addList("bearerAuth")));
        }
    }

    private void enrichDescription(Operation operation, OwnerOrAdminOperation ann) {
        String original = operation.getDescription() == null ? "" : operation.getDescription().trim();

        String accessLine = ann.description().isBlank()
                ? "Access: Resource owner or ADMIN only."
                : ann.description().trim();

        StringBuilder sb = new StringBuilder(accessLine);
        sb.append("\n\n");
        sb.append("Protected resource: ").append(ann.resource().name()).append(".");
        sb.append("\n");
        sb.append("Ownership parameter: ").append(ann.param()).append(".");

        if (!original.isBlank()) {
            sb.append("\n\n").append(original);
        }

        operation.setDescription(sb.toString());
    }

    private void ensure401(Operation operation) {
        if (operation.getResponses() == null) {
            operation.setResponses(new io.swagger.v3.oas.models.responses.ApiResponses());
        }
        if (operation.getResponses().get("401") == null) {
            operation.getResponses().addApiResponse("401",
                    new ApiResponse().description("Authentication required"));
        }
    }

    private void ensure403(Operation operation) {
        if (operation.getResponses().get("403") == null) {
            operation.getResponses().addApiResponse("403",
                    new ApiResponse().description("Authenticated but not allowed for this resource"));
        }
    }

    private void addExtensions(Operation operation, OwnerOrAdminOperation ann) {
        Map<String, Object> extensions = operation.getExtensions();
        if (extensions == null) {
            extensions = new LinkedHashMap<>();
            operation.setExtensions(extensions);
        }

        extensions.put("x-access-level", "OWNER_OR_ADMIN");
        extensions.put("x-owner-resource", ann.resource().name());
        extensions.put("x-owner-param", ann.param());
    }
}