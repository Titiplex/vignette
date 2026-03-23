package org.titiplex.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private Path root = Path.of("./data/storage");

    public Path getRoot() {
        return root;
    }

    public void setRoot(Path root) {
        this.root = root;
    }
}