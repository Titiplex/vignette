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
public class ThumbnailApiController {

    private final ThumbnailService thumbnailService;
    private final UserService userService;
    private final ScenarioService scenarioService;

    public ThumbnailApiController(ThumbnailService thumbnailService, UserService userService, ScenarioService scenarioService) {
        this.thumbnailService = thumbnailService;
        this.userService = userService;
        this.scenarioService = scenarioService;
    }

    public record ThumbnailRowDto(Long id, String title) {
    }

    public record UploadResponse(Long id) {
    }

    // public list
    @GetMapping("/api/scenarios/{scenarioId}/thumbnails")
    public List<ThumbnailRowDto> list(@PathVariable Long scenarioId) {
        return thumbnailService.listByScenarioId(scenarioId).stream()
                .map(t -> new ThumbnailRowDto(t.getId(), t.getTitle()))
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @scenarioSecurity.isOwner(#scenarioId, authentication.name))")
    @PostMapping(value = "/api/thumbnails", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

    // serve image
    @GetMapping("/api/thumbnails/{id}/content")
    public ResponseEntity<byte[]> content(@PathVariable Long id) {
        Thumbnail t = thumbnailService.getThumbnailById(id);

        String ct = t.getContentType();
        if (ct == null || ct.isBlank()) ct = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(ct))
                .body(t.getImageBytes());
    }
}