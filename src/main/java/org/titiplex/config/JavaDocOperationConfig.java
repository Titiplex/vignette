package org.titiplex.config;

import com.github.therapi.runtimejavadoc.*;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.customizers.PropertyCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Optional;

@Component
public class JavaDocOperationConfig implements OperationCustomizer {

    private static final CommentFormatter FORMATTER = new CommentFormatter();

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Class<?> declaringClass = handlerMethod.getBeanType();
        Method method = handlerMethod.getMethod();
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();

        // Get JavaDoc for the class
        ClassJavadoc classJavadoc = RuntimeJavadoc.getJavadoc(declaringClass);
        if (classJavadoc == null) {
            return operation;
        }

        // Find the method JavaDoc
        Optional<MethodJavadoc> methodJavadocOpt = classJavadoc.getMethods().stream()
                .filter(m -> m.getName().equals(methodName))
                .findFirst();

        if (methodJavadocOpt.isEmpty()) {
            return operation;
        }

        MethodJavadoc methodJavadoc = methodJavadocOpt.get();

        // Set operation description from method comment
        if (methodJavadoc.getComment() != null && !methodJavadoc.getComment().getElements().isEmpty()) {
            String description = FORMATTER.format(methodJavadoc.getComment());

            // If no summary is set, use first sentence as summary
            if (operation.getSummary() == null || operation.getSummary().isEmpty()) {
                String summary = extractFirstSentence(description);
                operation.setSummary(summary);
            }

            // Set full description
            if (operation.getDescription() == null || operation.getDescription().isEmpty()) {
                operation.setDescription(description);
            }
        }

        // Enhance parameters with JavaDoc @param descriptions
        java.lang.reflect.Parameter[] methodParams = method.getParameters();
        if (operation.getParameters() != null) {
            for (Parameter parameter : operation.getParameters()) {
                String paramName = parameter.getName();

                methodJavadoc.getParams().stream()
                        .filter(p -> p.getName().equals(paramName))
                        .findFirst()
                        .ifPresent(paramDoc -> {
                            String paramDescription = FORMATTER.format(paramDoc.getComment());
                            // Clean up {@link ...} tags
                            paramDescription = cleanJavaDocTags(paramDescription);
                            if (parameter.getDescription() == null || parameter.getDescription().isEmpty()) {
                                parameter.setDescription(paramDescription);
                            }
                        });
            }
        }

        // Handle @RequestBody parameters
        for (java.lang.reflect.Parameter methodParam : methodParams) {
            if (methodParam.isAnnotationPresent(RequestBody.class)) {
                String paramName = methodParam.getName();
                methodJavadoc.getParams().stream()
                        .filter(p -> p.getName().equals(paramName))
                        .findFirst()
                        .ifPresent(paramDoc -> {
                            String paramDescription = FORMATTER.format(paramDoc.getComment());
                            paramDescription = cleanJavaDocTags(paramDescription);

                            // Add to request body description
                            if (operation.getRequestBody() != null) {
                                if (operation.getRequestBody().getDescription() == null
                                        || operation.getRequestBody().getDescription().isEmpty()) {
                                    operation.getRequestBody().setDescription(paramDescription);
                                }
                            }
                        });
            }
        }

        // Add @return documentation to response description
        if (methodJavadoc.getReturns() != null && !methodJavadoc.getReturns().getElements().isEmpty()) {
            String returnDoc = FORMATTER.format(methodJavadoc.getReturns());
            returnDoc = cleanJavaDocTags(returnDoc);

            if (operation.getResponses() != null && operation.getResponses().get("200") != null) {
                var response = operation.getResponses().get("200");
                if (response.getDescription() == null || response.getDescription().isEmpty()
                        || response.getDescription().equals("OK")) {
                    response.setDescription(returnDoc);
                }
            }
        }

        return operation;
    }

    private String extractFirstSentence(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // Clean JavaDoc tags first
        text = cleanJavaDocTags(text);

        // Find first period followed by space or end of string
        int endIdx = text.indexOf(". ");
        if (endIdx == -1) {
            endIdx = text.indexOf(".\n");
        }
        if (endIdx == -1) {
            endIdx = text.indexOf("\n\n");
        }

        if (endIdx > 0) {
            return text.substring(0, endIdx + 1).trim();
        }

        // Return first 100 chars or full text
        return text.length() > 100 ? text.substring(0, 100) + "..." : text;
    }

    private String cleanJavaDocTags(String text) {
        if (text == null) {
            return null;
        }

        // Remove {@link ClassName} and replace with just ClassName
        text = text.replaceAll("\\{@link\\s+([^}]+)}", "$1");

        // Remove {@code ...}
        text = text.replaceAll("\\{@code\\s+([^}]+)}", "`$1`");

        // Remove other common tags
        text = text.replaceAll("\\{@[^}]+}", "");

        return text.trim();
    }

    /**
     * Bean to customize property descriptions from field JavaDoc.
     * This adds JavaDoc from DTO/entity fields to the OpenAPI schema properties.
     */
    @Bean
    public PropertyCustomizer propertyCustomizer() {
        return (schema, type) -> {
            // Get the Java class for this property
            Class<?> rawClass = type.getClass();
            if (rawClass == null) {
                return schema;
            }

            // Get JavaDoc for the class
            ClassJavadoc classJavadoc = RuntimeJavadoc.getJavadoc(rawClass);
            if (classJavadoc == null) {
                return schema;
            }

            // If this is a class schema (not a simple property), add class description
            if (schema.getDescription() == null || schema.getDescription().isEmpty()) {
                if (classJavadoc.getComment() != null && !classJavadoc.getComment().getElements().isEmpty()) {
                    String classDescription = FORMATTER.format(classJavadoc.getComment());
                    schema.setDescription(cleanJavaDocTags(classDescription));
                }
            }

            // Enhance properties with field JavaDoc
            if (schema.getProperties() != null && !schema.getProperties().isEmpty()) {
                schema.getProperties().forEach((propertyName, propertySchema) -> {
                    // Cast propertySchema to Schema
                    if (!(propertySchema instanceof Schema<?> property)) {
                        return;
                    }

                    // Find the field JavaDoc
                    Optional<FieldJavadoc> fieldJavadocOpt = classJavadoc.getFields().stream()
                            .filter(f -> f.getName().equals(propertyName))
                            .findFirst();

                    if (fieldJavadocOpt.isPresent()) {
                        FieldJavadoc fieldJavadoc = fieldJavadocOpt.get();
                        if (fieldJavadoc.getComment() != null && !fieldJavadoc.getComment().getElements().isEmpty()) {
                            String fieldDescription = FORMATTER.format(fieldJavadoc.getComment());
                            fieldDescription = cleanJavaDocTags(fieldDescription);

                            // Only set if not already set
                            if (property.getDescription() == null
                                    || property.getDescription().isEmpty()) {
                                property.setDescription(fieldDescription);
                            }
                        }
                    } else {
                        // Try to find getter method JavaDoc as fallback
                        String getterName = "get" + capitalize((String) propertyName);
                        String isGetterName = "is" + capitalize((String) propertyName);

                        Optional<MethodJavadoc> getterJavadocOpt = classJavadoc.getMethods().stream()
                                .filter(m -> m.getName().equals(getterName) || m.getName().equals(isGetterName))
                                .filter(m -> m.getParams().isEmpty()) // Getter has no parameters
                                .findFirst();

                        if (getterJavadocOpt.isPresent()) {
                            MethodJavadoc getterJavadoc = getterJavadocOpt.get();

                            // Use @return documentation for getter
                            if (getterJavadoc.getReturns() != null && !getterJavadoc.getReturns().getElements().isEmpty()) {
                                String getterDescription = FORMATTER.format(getterJavadoc.getReturns());
                                getterDescription = cleanJavaDocTags(getterDescription);

                                if (property.getDescription() == null
                                        || property.getDescription().isEmpty()) {
                                    property.setDescription(getterDescription);
                                }
                            } else if (getterJavadoc.getComment() != null && !getterJavadoc.getComment().getElements().isEmpty()) {
                                // Fallback to method comment if no @return
                                String getterDescription = FORMATTER.format(getterJavadoc.getComment());
                                getterDescription = cleanJavaDocTags(getterDescription);

                                if (property.getDescription() == null
                                        || property.getDescription().isEmpty()) {
                                    property.setDescription(getterDescription);
                                }
                            }
                        }
                    }
                });
            }

            return schema;
        };
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}