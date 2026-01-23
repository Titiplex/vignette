package org.titiplex.api.dto;

import org.springframework.web.multipart.MultipartFile;

public record ImageUploadForm(String title, MultipartFile image, Long scenarioId) {
}
