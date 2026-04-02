package org.titiplex.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.titiplex.api.dto.*;
import org.titiplex.api.security.OwnerOrAdminOperation;
import org.titiplex.api.security.ProtectedResource;
import org.titiplex.api.security.PublicOperation;
import org.titiplex.api.security.UserOperation;
import org.titiplex.persistence.model.Scenario;
import org.titiplex.service.LanguageService;
import org.titiplex.service.ScenarioService;
import org.titiplex.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/scenarios")
@Tag(
        name = "Scenario",
        description = "Endpoints for creating, retrieving, listing, and deleting scenarios."
)
public class ScenarioApiController {

    private final ScenarioService scenarioService;
    private final UserService userService;
    private final LanguageService languageService;

    public ScenarioApiController(ScenarioService scenarioService, UserService userService, LanguageService languageService) {
        this.scenarioService = scenarioService;
        this.userService = userService;
        this.languageService = languageService;
    }

    /**
     * Creates a new scenario based on the provided details.
     *
     * @param req  the request object {@link CreateScenarioRequest} containing the title, description, and language ID of the scenario
     * @param auth the authentication object representing the currently authenticated user
     * @return a response object {@link CreateScenarioResponse} containing the ID of the newly created scenario
     * @throws IllegalArgumentException if the title or language ID is null, blank, or invalid,
     *                                  or if a scenario with the same title, language, and author already exists
     */
    @Operation(
            summary = "Create a new scenario",
            description = "Creates a new scenario for the authenticated user."
    )
    @UserOperation
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Scenario created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateScenarioResponse.class),
                            examples = @ExampleObject(
                                    name = "Created Scenario Response",
                                    value = "{\"id\": 42}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input : missing required fields, unknown language ID, or duplicate scenario",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied : USER role required",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Language not found with the specified ID",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict : scenario with same title, language, and author already exists",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateScenarioResponse create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Scenario creation details including title, description, and language ID",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CreateScenarioRequest.class)
                    )
            )
            @RequestBody CreateScenarioRequest req,

            @Parameter(hidden = true)
            Authentication auth) {
        if (req.title() == null || req.title().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (req.languageId() == null || req.languageId().isBlank()) {
            throw new IllegalArgumentException("Language is required");
        }
        if (!languageService.existsById(req.languageId())) {
            throw new IllegalArgumentException("Unknown language id");
        }

        var username = auth.getName();
        Long userId = userService.getUserByUsername(username).getId();

        if (scenarioService.existsByTitleAndAuthorNameAndLanguageId(req.title(), username, req.languageId())) {
            throw new IllegalArgumentException("Scenario already exists for this user and language");
        }

        Long id = scenarioService.createScenario(req.title().trim(), req.description(), userId, req.languageId()).getId();
        return new CreateScenarioResponse(id);
    }

    /**
     * Retrieves a specific scenario based on the provided ID.
     *
     * @param id ({@link Long}) the unique identifier of the scenario to retrieve
     * @return a {@link ScenarioDto} representing the scenario matching the provided ID
     */
    @Operation(
            summary = "Get a scenario by ID",
            description = "Retrieves detailed information about a specific scenario including its thumbnails and metadata."
    )
    @PublicOperation
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Scenario retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ScenarioDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Scenario not found with the specified ID",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{id}")
    public ScenarioDto getOne(
            @Parameter(
                    description = "ID of the scenario to retrieve",
                    required = true
            )
            @PathVariable Long id,

            @Parameter(hidden = true)
            Authentication auth
    ) {
        Scenario s = scenarioService.getVisibleScenario(id, auth);

        return scenarioService.toDto(s);
    }

    /**
     * Retrieves a list of all scenarios and converts them into DTOs.
     *
     * @return a list of {@link ScenarioDto} objects representing all scenarios,
     * ordered by creation date in descending order
     */
    @Operation(
            summary = "List all scenarios",
            description = "Retrieves a list of all scenarios in the system, ordered by creation date (most recent first)."
    )
    @PublicOperation
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Scenarios retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ScenarioDto.class))
                    )
            )
    })
    @GetMapping
    public List<ScenarioDto> listAll(
            @Parameter(hidden = true)
            Authentication auth
    ) {
        return scenarioService.listVisibleScenarios(auth).stream().map(scenarioService::toDto).toList();
    }

    @OwnerOrAdminOperation(
            resource = ProtectedResource.SCENARIO
    )
    @PatchMapping("/{id}/storyboard")
    public ScenarioDto updateStoryboard(
            @PathVariable Long id,
            @RequestBody UpdateScenarioStoryboardRequest req,

            @Parameter(hidden = true)
            Authentication auth
    ) {
        return scenarioService.toDto(scenarioService.updateStoryboard(id, req, auth));
    }

    @OwnerOrAdminOperation(
            resource = ProtectedResource.SCENARIO
    )
    @PostMapping("/{id}/publish")
    public ScenarioDto publish(
            @PathVariable Long id,

            @Parameter(hidden = true)
            Authentication auth
    ) {
        return scenarioService.toDto(scenarioService.publishScenario(id, auth));
    }

    /**
     * Deletes a scenario based on the provided ID. The operation is authorized
     * for users with the 'ADMIN' role or for the owner of the specified scenario.
     *
     * @param id ({@link Long}) the identification of the scenario to delete
     */
    @Operation(
            summary = "Delete a scenario",
            description = "Deletes a scenario and all its associated data (thumbnails, audios, etc.)."
    )
    @OwnerOrAdminOperation(
            resource = ProtectedResource.SCENARIO
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Scenario deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Forbidden : user is not the owner or admin",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Scenario not found with the specified ID",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
//    @ApiAccess(
//            level = ApiAccessLevel.OWNER_OR_ADMIN,
//            rule = "Requires authentication. Authorization: scenario owner or ADMIN only.",
//            ownerResource = "scenario"
//    )
//    @SecurityRequirement(name = "bearerAuth")
//    @PreAuthorize("hasRole('ADMIN') or @scenarioSecurity.isOwner(#id, authentication.name)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(
                    description = "ID of the scenario to delete",
                    required = true
            )
            @PathVariable Long id
    ) {
        scenarioService.deleteScenario(id);
    }
}