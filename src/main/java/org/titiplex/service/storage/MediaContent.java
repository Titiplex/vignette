package org.titiplex.service.storage;

import org.springframework.core.io.Resource;

public record MediaContent(
        Resource resource,
        String contentType,
        long sizeBytes,
        String etag
) {
}