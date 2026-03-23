package org.titiplex.service.storage;

public record StoredFile(
        String relativePath,
        String sha256,
        long sizeBytes,
        String contentType,
        String originalFilename
) {
}