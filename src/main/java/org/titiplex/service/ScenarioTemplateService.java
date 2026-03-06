package org.titiplex.service;

import org.springframework.stereotype.Service;
import org.titiplex.api.dto.ScenarioTemplateDto;
import org.titiplex.persistence.model.Scenario;
import org.titiplex.persistence.model.ScenarioTemplate;
import org.titiplex.persistence.repo.ScenarioTemplateRepository;

import java.util.List;

@Service
public class ScenarioTemplateService {

    private final ScenarioTemplateRepository templateRepository;
    private final ScenarioService scenarioService;
    private final UserService userService;

    public ScenarioTemplateService(
            ScenarioTemplateRepository templateRepository,
            ScenarioService scenarioService,
            UserService userService
    ) {
        this.templateRepository = templateRepository;
        this.scenarioService = scenarioService;
        this.userService = userService;
    }

    public ScenarioTemplateDto publishTemplate(Long scenarioId, String username) {
        Scenario scenario = scenarioService.getScenario(scenarioId);
        if (scenario.getId() == null) {
            throw new IllegalArgumentException("Scenario not found");
        }

        String author = userService.getUserById(scenario.getAuthor_id()).getUsername();
        if (!author.equals(username)) {
            throw new IllegalArgumentException("Only the scenario owner can publish a template");
        }

        ScenarioTemplate template = templateRepository.save(
                scenario.getId(),
                scenario.getTitle(),
                scenario.getDescription(),
                scenario.getLanguage_id(),
                author
        );

        return toDto(template);
    }

    public List<ScenarioTemplateDto> listTemplates(String languageId) {
        return templateRepository.findAll(languageId).stream().map(ScenarioTemplateService::toDto).toList();
    }

    public Long createScenarioFromTemplate(Long templateId, String title, String username) {
        ScenarioTemplate template = templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        Long userId = userService.getUserByUsername(username).getId();
        String normalizedTitle = (title == null || title.isBlank()) ? template.title() : title.trim();

        if (scenarioService.existsByTitleAndAuthorNameAndLanguageId(normalizedTitle, username, template.languageId())) {
            throw new IllegalArgumentException("Scenario already exists for this user and language");
        }

        return scenarioService
                .createScenario(normalizedTitle, template.description(), userId, template.languageId())
                .getId();
    }

    public static ScenarioTemplateDto toDto(ScenarioTemplate template) {
        return new ScenarioTemplateDto(
                template.id(),
                template.sourceScenarioId(),
                template.title(),
                template.description(),
                template.languageId(),
                template.sourceAuthor(),
                template.createdAt()
        );
    }
}
