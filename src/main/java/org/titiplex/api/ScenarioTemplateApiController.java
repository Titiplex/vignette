package org.titiplex.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.titiplex.api.dto.ScenarioTemplateDto;
import org.titiplex.service.ScenarioTemplateService;

import java.util.List;

@RestController
@RequestMapping("/api/template-scenarios")
public class ScenarioTemplateApiController {

    private final ScenarioTemplateService templateService;

    public ScenarioTemplateApiController(ScenarioTemplateService templateService) {
        this.templateService = templateService;
    }

    public record CreateTemplateResponse(Long id) {
    }

    public record CreateFromTemplateRequest(String title) {
    }

    public record CreateFromTemplateResponse(Long id) {
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{scenarioId}")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTemplateResponse publishTemplate(@PathVariable Long scenarioId, Authentication auth) {
        var template = templateService.publishTemplate(scenarioId, auth.getName());
        return new CreateTemplateResponse(template.id());
    }

    @GetMapping
    public List<ScenarioTemplateDto> listTemplates(@RequestParam(required = false) String languageId) {
        return templateService.listTemplates(languageId);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{templateId}/instantiate")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateFromTemplateResponse instantiate(
            @PathVariable Long templateId,
            @RequestBody(required = false) CreateFromTemplateRequest req,
            Authentication auth
    ) {
        String title = req == null ? null : req.title();
        Long scenarioId = templateService.createScenarioFromTemplate(templateId, title, auth.getName());
        return new CreateFromTemplateResponse(scenarioId);
    }
}
