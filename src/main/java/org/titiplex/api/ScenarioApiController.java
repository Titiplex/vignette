package org.titiplex.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.titiplex.persistence.model.Scenario;
import org.titiplex.service.LanguageService;
import org.titiplex.service.ScenarioService;
import org.titiplex.service.UserService;

import java.time.Instant;

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

    public record ScenarioDto(
            Long id,
            String title,
            String description,
            String languageId,
            String authorUsername,
            Instant createdAt
    ) {
    }

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

    @GetMapping("/{id}")
    public ScenarioDto getOne(@PathVariable Long id) {
        Scenario s = scenarioService.getScenario(id);

        return new ScenarioDto(
                s.getId(),
                s.getTitle(),
                s.getDescription(),
                s.getLanguage_id(),
                s.getAuthor().getUsername(),
                s.getCreatedAt()
        );
    }

    @PreAuthorize("hasRole('ADMIN') or @scenarioSecurity.isOwner(#id, authentication.name)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        scenarioService.deleteScenario(id);
    }
}