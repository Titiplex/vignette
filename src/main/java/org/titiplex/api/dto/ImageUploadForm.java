package org.titiplex.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(name = "ImageUploadForm", description = "Form data for image upload")
public record ImageUploadForm(
        @Schema(description = "Title of the image", example = "My Image")
        String title,
        @Schema(description = "Image file to upload")
        MultipartFile image,
        @Schema(description = "ID of the scenario to which the image belongs")
        Long scenarioId) {
}
