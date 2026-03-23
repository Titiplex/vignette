package org.titiplex.config;

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
            // Create a TreeMap to hold paths, which sorts them alphabetically by key (path string)
            Map<String, io.swagger.v3.oas.models.PathItem> sortedPaths = new TreeMap<>(Comparator.naturalOrder());
            sortedPaths.putAll(openApi.getPaths());
            var paths = new io.swagger.v3.oas.models.Paths();
            paths.putAll(sortedPaths);
            openApi.setPaths(paths);

            // You can also apply sorting within each PathItem's operations if needed
//            openApi.getPaths().values().forEach(pathItem -> {
//                // Example for sorting operations within a path (this is more complex to implement in Java directly for the UI display order, which is client-side)
//            });
        };
    }
}
