package org.titiplex.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.titiplex.api.dto.ThumbnailRowDto;
import org.titiplex.api.dto.UploadResponse;
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
@Tag(
        name = "Thumbnail Endpoint",
        description = "Endpoints for managing thumbnail images associated with scenarios, including listing, uploading, and retrieving thumbnail content."
)
public class ThumbnailApiController {

    private final ThumbnailService thumbnailService;
    private final UserService userService;
    private final ScenarioService scenarioService;

    public ThumbnailApiController(ThumbnailService thumbnailService, UserService userService, ScenarioService scenarioService) {
        this.thumbnailService = thumbnailService;
        this.userService = userService;
        this.scenarioService = scenarioService;
    }

    /**
     * Retrieves a list of thumbnails associated with a specific scenario ID.
     *
     * @param scenarioId the ID ({@link Long}) of the scenario for which thumbnails are to be retrieved
     * @return a {@link List} of {@link ThumbnailRowDto} objects representing the thumbnails of the specified scenario
     */
    @Operation(
            summary = "List thumbnails for a scenario",
            description = "Retrieves all thumbnails associated with a specific scenario, ordered by their index."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Thumbnails retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ThumbnailRowDto.class)),
                            examples = @ExampleObject(
                                    name = "Example Thumbnail List",
                                    value = "[{\"id\": 1, \"title\": \"Scene 1\", \"idx\": 1}, {\"id\": 2, \"title\": \"Scene 2\", \"idx\": 2}]"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Scenario not found with the specified ID"
            )
    })
    @GetMapping("/scenarios/{scenarioId}/thumbnails")
    public List<ThumbnailRowDto> list(
            @Parameter(
                    description = "ID of the scenario to retrieve thumbnails for",
                    required = true
            )
            @PathVariable Long scenarioId
    ) {
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
    @Operation(
            summary = "Upload a thumbnail image",
            description = "Uploads a new thumbnail image for a scenario. " +
                    "Requires authentication and either ADMIN role or ownership of the scenario. " +
                    "The image will be processed and associated with the specified scenario.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Thumbnail uploaded successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UploadResponse.class),
                            examples = @ExampleObject(
                                    name = "Upload Success Response",
                                    value = "{\"id\": 42}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input - missing or empty image file"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden - user is not the scenario owner or admin"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Scenario not found with the specified ID"
            ),
            @ApiResponse(
                    responseCode = "415",
                    description = "Unsupported media type - invalid image format"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - I/O error during image processing"
            )
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and @scenarioSecurity.isOwner(#scenarioId, authentication.name))")
    @PostMapping(value = "/thumbnails", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UploadResponse upload(
            @Parameter(
                    description = "ID of the scenario to associate the thumbnail with",
                    required = true
            )
            @RequestParam Long scenarioId,

            @Parameter(
                    description = "Optional title for the thumbnail",
                    example = "Introduction Scene"
            )
            @RequestParam(required = false, defaultValue = "") String title,

            @Parameter(
                    description = "Image file to upload (JPEG, PNG, etc.)",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("image") MultipartFile image,

            @Parameter(hidden = true)
            Authentication auth
    ) throws IOException {

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
    @Operation(
            summary = "Get thumbnail image content",
            description = "Retrieves the binary content of a thumbnail image with appropriate content type headers and caching. " +
                    "Returns the raw image data as bytes."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Thumbnail content retrieved successfully",
                    content = @Content(
                            mediaType = "image/*",
                            schema = @Schema(type = "string", format = "binary")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Thumbnail not found with the specified ID"
            )
    })
    @GetMapping("/thumbnails/{id}/content")
    public ResponseEntity<byte[]> content(
            @Parameter(
                    description = "ID of the thumbnail to retrieve",
                    required = true,
                    example = "1"
            )
            @PathVariable Long id
    ) {
        Thumbnail t = thumbnailService.getThumbnailById(id);

        String ct = t.getContentType();
        if (ct == null || ct.isBlank()) ct = MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(ct))
                .body(t.getImageBytes());
    }
}