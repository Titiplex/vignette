package org.titiplex.config.components;

import org.springframework.stereotype.Component;
import org.titiplex.service.ScenarioService;

@Component
public class ScenarioSecurity {
    private final ScenarioService scenarioService;

    public ScenarioSecurity(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }
    public boolean isOwner(Long scenarioId, String username) {
        return scenarioService.existsByIdAndAuthorUsername(scenarioId, username);
    }
}
