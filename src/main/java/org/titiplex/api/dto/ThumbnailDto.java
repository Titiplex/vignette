package org.titiplex.api.dto;

public record ThumbnailDto(
        Long id,
        String title,
        Long authorId,
        byte[] imageBytes,
        String imageSha256
) {
}
