package org.titiplex.service;

import org.springframework.stereotype.Service;
import org.titiplex.persistence.model.Scenario;
import org.titiplex.persistence.repo.ScenarioRepository;

import java.util.List;

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
        return this.repo.save(scenario);
    }

    public Scenario getScenario(Long id) {
        Scenario scenario = new Scenario();
        scenario.setTitle("Scenario not found");
        return repo.findById(id).orElse(scenario);
    }

    public void deleteScenario(Long id) {
        repo.findById(id).ifPresent(repo::delete);
    }

    public List<Scenario> listScenarios() {
        return repo.findAllByOrderByCreatedAtDesc();
    }
}
