package org.titiplex.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.titiplex.persistence.model.Scenario;
import org.titiplex.persistence.model.Thumbnail;
import org.titiplex.persistence.model.User;
import org.titiplex.service.ScenarioService;
import org.titiplex.service.ThumbnailService;
import org.titiplex.service.UserService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ThumbnailApiController {

    private final ThumbnailService thumbnailService;
    private final UserService userService;
    private final ScenarioService scenarioService;

    public ThumbnailApiController(ThumbnailService thumbnailService, UserService userService, ScenarioService scenarioService) {
        this.thumbnailService = thumbnailService;
        this.userService = userService;
        this.scenarioService = scenarioService;
    }

    public record ThumbnailRowDto(Long id, String title, Integer idx) {
    }

    public record UploadResponse(Long id) {
    }

    /**
     * Retrieves a list of thumbnails associated with a specific scenario ID.
     *
     * @param scenarioId the ID ({@link Long}) of the scenario for which thumbnails are to be retrieved
     * @return a {@link List} of {@link ThumbnailRowDto} objects representing the thumbnails of the specified scenario
     */
    // public list
    @GetMapping("/scenarios/{scenarioId}/thumbnails")
    public List<ThumbnailRowDto> list(@PathVariable Long scenarioId) {
        return thumbnailService.listByScenarioId(scenarioId).stream()
                .map(t -> new ThumbnailRowDto(t.getId(), t.getTitle(), t.getIdx()))
                .toList();
    }

    /**
     * Uploads a thumbnail image for a specified scenario. The method requires proper authorization.
     * Users with 'ADMIN' role or users with 'USER' role who are owners of the specified scenario can upload thumbnails.
     *
     * @param scenarioId the ID ({@link Long}) of the scenario to which the thumbnail is associated
     * @param title      an optional title for the uploaded thumbnail; defaults to an empty string if not provided
     * @param image      the image file ({@link MultipartFile}) to be uploaded as a thumbnail; must not be null or empty
     * @param auth       the authentication object representing the currently authenticated user
     * @return an {@link UploadResponse} containing the ID of the newly uploaded thumbnail
     * @throws IOException              if an I/O error occurs during image processing or saving
     * @throws IllegalArgumentException if the provided image file is null or empty
     */
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @scenarioSecurity.isOwner(#scenarioId, authentication.name))")
    @PostMapping(value = "/thumbnails", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadResponse upload(@RequestParam Long scenarioId,
                                 @RequestParam(required = false, defaultValue = "") String title,
                                 @RequestPart("image") MultipartFile image,
                                 Authentication auth) throws IOException {

        if (image == null || image.isEmpty()) throw new IllegalArgumentException("Image is required");

        User user = userService.getUserByUsername(auth.getName());
        Scenario scenario = scenarioService.getScenario(scenarioId);

        Thumbnail saved = thumbnailService.save(title, image, scenario, user);
        return new UploadResponse(saved.getId());
    }

    /**
     * Retrieves the binary content of a thumbnail image by its ID.
     *
     * @param id the ID ({@link Long}) of the thumbnail whose binary content is to be retrieved
     * @return a {@link ResponseEntity} containing the binary content ( byte array ) of the thumbnail image
     * with the appropriate content type header, or the default content type
     * of "application/octet-stream" if none is specified
     */
    // serve image
    @GetMapping("/thumbnails/{id}/content")
    public ResponseEntity<byte[]> content(@PathVariable Long id) {
        Thumbnail t = thumbnailService.getThumbnailById(id);

        String ct = t.getContentType();
        if (ct == null || ct.isBlank()) ct = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(ct))
                .body(t.getImageBytes());
    }
}