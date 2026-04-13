package org.titiplex.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.titiplex.api.dto.ScenarioTagSuggestionDto;
import org.titiplex.api.security.PublicOperation;
import org.titiplex.service.ScenarioTagService;

import java.util.List;

@RestController
@RequestMapping("/api/scenario/tags")
public class ScenarioTagApiController {

    private final ScenarioTagService scenarioTagService;

    public ScenarioTagApiController(ScenarioTagService scenarioTagService) {
        this.scenarioTagService = scenarioTagService;
    }

    @GetMapping
    @PublicOperation
    public List<ScenarioTagSuggestionDto> suggest(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "10") int limit
    ) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        return scenarioTagService.suggest(q, safeLimit);
    }
}