package org.titiplex.config;

import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

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
                Map<String, PathItem> sortedPaths =
                        new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
                sortedPaths.putAll(openApi.getPaths());

                Paths paths = new Paths();
                sortedPaths.forEach(paths::addPathItem);

                openApi.setPaths(paths);
            }
        };
    }
}