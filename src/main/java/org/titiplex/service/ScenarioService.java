package org.titiplex.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.titiplex.api.dto.ScenarioDto;
import org.titiplex.api.dto.UpdateScenarioStoryboardRequest;
import org.titiplex.persistence.model.Scenario;
import org.titiplex.persistence.model.ScenarioVisibilityStatus;
import org.titiplex.persistence.model.StoryboardLayoutMode;
import org.titiplex.persistence.repo.ScenarioRepository;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ScenarioService {
    private final ScenarioRepository repo;
    private final UserService userService;
    private final LanguageService languageService;

    public ScenarioService(ScenarioRepository scenarioRepository, UserService userService, LanguageService languageService) {
        this.repo = scenarioRepository;
        this.userService = userService;
        this.languageService = languageService;
    }

    public boolean existsByIdAndAuthorUsername(Long scenarioId, String username) {
        return repo.existsByIdAndAuthorUsername(scenarioId, username);
    }

    public boolean existsByTitleAndAuthorNameAndLanguageId(String title, String authorName, String languageId) {
        return repo.existsByTitleAndAuthorUsernameAndLanguageId(title, authorName, languageId);
    }

    public Scenario createScenario(String title, String description, Long authorId, String languageId) {
        Scenario scenario = new Scenario();
        scenario.setTitle(title);
        scenario.setDescription(description);
        scenario.setAuthor_id(authorId);
        scenario.setLanguage_id(languageId);
        scenario.setAuthor(userService.getUserById(authorId));
        scenario.setLanguage(languageService.getLanguage(languageId));
        scenario.setVisibilityStatus(ScenarioVisibilityStatus.DRAFT);
        scenario.setStoryboardLayoutMode(StoryboardLayoutMode.PRESET);
        scenario.setStoryboardPreset("GRID_3");
        scenario.setStoryboardColumns(3);
        return repo.save(scenario);
    }

    public Scenario getRequiredScenario(Long id) {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Scenario not found"));
    }

    public Scenario getVisibleScenario(Long id, Authentication authentication) {
        Scenario scenario = getRequiredScenario(id);
        assertCanViewScenario(scenario, authentication);
        return scenario;
    }

    public List<Scenario> listVisibleScenarios(Authentication authentication) {
        if (isAdmin(authentication)) {
            return repo.findAllByOrderByCreatedAtDesc();
        }

        String username = authenticatedUsername(authentication);
        if (username == null) {
            return repo.findAllByVisibilityStatusOrderByCreatedAtDesc(ScenarioVisibilityStatus.PUBLISHED);
        }

        return repo.findVisibleToUsername(username);
    }

    public Scenario publishScenario(Long id, Authentication authentication) {
        Scenario scenario = getRequiredScenario(id);
        assertCanEditScenario(scenario, authentication);

        scenario.setVisibilityStatus(ScenarioVisibilityStatus.PUBLISHED);
        if (scenario.getPublishedAt() == null) {
            scenario.setPublishedAt(Instant.now());
        }

        return repo.save(scenario);
    }

    public Scenario updateStoryboard(Long id, UpdateScenarioStoryboardRequest request, Authentication authentication) {
        Scenario scenario = getRequiredScenario(id);
        assertCanEditScenario(scenario, authentication);

        if (request.layoutMode() != null) {
            scenario.setStoryboardLayoutMode(StoryboardLayoutMode.valueOf(request.layoutMode().trim().toUpperCase()));
        }

        if (request.preset() != null && !request.preset().isBlank()) {
            scenario.setStoryboardPreset(request.preset().trim().toUpperCase());
        }

        if (request.columns() != null) {
            int columns = request.columns();
            if (columns < 1 || columns > 8) {
                throw new IllegalArgumentException("Storyboard columns must be between 1 and 8");
            }
            scenario.setStoryboardColumns(columns);
        }

        return repo.save(scenario);
    }

    public void deleteScenario(Long id) {
        repo.findById(id).ifPresent(repo::delete);
    }

    public void assertCanViewScenario(Scenario scenario, Authentication authentication) {
        if (scenario.getVisibilityStatus() == ScenarioVisibilityStatus.PUBLISHED) {
            return;
        }
        if (isAdmin(authentication)) {
            return;
        }

        String username = authenticatedUsername(authentication);
        if (username != null && username.equalsIgnoreCase(scenario.getAuthor().getUsername())) {
            return;
        }

        throw new NoSuchElementException("Scenario not found");
    }

    public void assertCanEditScenario(Scenario scenario, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new InsufficientAuthenticationException("Authentication required");
        }
        if (isAdmin(authentication)) {
            return;
        }

        String username = authenticatedUsername(authentication);
        if (username != null && username.equalsIgnoreCase(scenario.getAuthor().getUsername())) {
            return;
        }

        throw new AccessDeniedException("You are not allowed to edit this scenario");
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities() != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    private String authenticatedUsername(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return authentication.getName();
    }

    public static ScenarioDto toDto(Scenario s) {
        return new ScenarioDto(
                s.getId(),
                s.getTitle(),
                s.getDescription(),
                s.getLanguage_id(),
                s.getAuthor().getUsername(),
                s.getCreatedAt(),
                s.getVisibilityStatus().name(),
                s.getPublishedAt(),
                s.getStoryboardLayoutMode().name(),
                s.getStoryboardPreset(),
                s.getStoryboardColumns()
        );
    }
}