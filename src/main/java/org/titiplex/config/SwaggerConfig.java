package org.titiplex.config;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;
import java.util.stream.Stream;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenApiCustomizer customOpenApiCustomizer() {
        return openApi -> {
            if (openApi.getTags() != null) {
                openApi.setTags(
                        openApi.getTags().stream()
                                .sorted(Comparator.comparing(
                                        Tag::getName,
                                        String.CASE_INSENSITIVE_ORDER
                                ))
                                .toList()
                );
            }

            if (openApi.getPaths() != null) {
                List<Map.Entry<String, PathItem>> sortedEntries =
                        new ArrayList<>(openApi.getPaths().entrySet());

                sortedEntries.sort(
                        Comparator
                                .comparing(
                                        (Map.Entry<String, PathItem> e) -> primaryTag(e.getValue()),
                                        String.CASE_INSENSITIVE_ORDER
                                )
                                .thenComparing(
                                        e -> primarySummary(e.getValue()),
                                        String.CASE_INSENSITIVE_ORDER
                                )
                                .thenComparing(
                                        Map.Entry::getKey,
                                        String.CASE_INSENSITIVE_ORDER
                                )
                );

                Paths sortedPaths = new Paths();
                sortedEntries.forEach(e -> sortedPaths.addPathItem(e.getKey(), e.getValue()));
                openApi.setPaths(sortedPaths);
            }
        };
    }

    private static String primaryTag(PathItem pathItem) {
        return readOperations(pathItem).stream()
                .flatMap(op -> {
                    List<String> tags = op.getTags();
                    return tags == null ? Stream.empty() : tags.stream();
                })
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .min(String.CASE_INSENSITIVE_ORDER)
                .orElse("");
    }

    private static String primarySummary(PathItem pathItem) {
        return readOperations(pathItem).stream()
                .map(op -> firstNonBlank(op.getSummary(), op.getOperationId()))
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .min(String.CASE_INSENSITIVE_ORDER)
                .orElse("");
    }

    private static List<Operation> readOperations(PathItem pathItem) {
        List<Operation> operations = pathItem.readOperations();
        return operations == null ? List.of() : operations;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }

        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return "";
    }
}