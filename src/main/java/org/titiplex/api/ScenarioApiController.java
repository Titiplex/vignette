package org.titiplex.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.titiplex.api.dto.ScenarioDto;
import org.titiplex.persistence.model.Scenario;
import org.titiplex.service.LanguageService;
import org.titiplex.service.ScenarioService;
import org.titiplex.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/scenarios")
public class ScenarioApiController {

    private final ScenarioService scenarioService;
    private final UserService userService;
    private final LanguageService languageService;

    public ScenarioApiController(ScenarioService scenarioService, UserService userService, LanguageService languageService) {
        this.scenarioService = scenarioService;
        this.userService = userService;
        this.languageService = languageService;
    }

    public record CreateScenarioRequest(String title, String description, String languageId) {
    }

    public record CreateScenarioResponse(Long id) {
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
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateScenarioResponse create(@RequestBody CreateScenarioRequest req, Authentication auth) {
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
    @GetMapping("/{id}")
    public ScenarioDto getOne(@PathVariable Long id) {
        Scenario s = scenarioService.getScenario(id);

        return ScenarioService.toDto(s);
    }

    /**
     * Retrieves a list of all scenarios and converts them into DTOs.
     *
     * @return a list of {@link ScenarioDto} objects representing all scenarios,
     * ordered by creation date in descending order
     */
    @GetMapping
    public List<ScenarioDto> listAll() {
        return scenarioService.listScenarios().stream().map(ScenarioService::toDto).toList();
    }

    /**
     * Deletes a scenario based on the provided ID. The operation is authorized
     * for users with the 'ADMIN' role or for the owner of the specified scenario.
     *
     * @param id ({@link Long}) the identification of the scenario to delete
     */
    @PreAuthorize("hasRole('ADMIN') or @scenarioSecurity.isOwner(#id, authentication.name)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        scenarioService.deleteScenario(id);
    }
}