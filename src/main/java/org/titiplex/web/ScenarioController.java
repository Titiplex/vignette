package org.titiplex.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.titiplex.api.dto.ScenarioForm;
import org.titiplex.service.LanguageService;
import org.titiplex.service.ScenarioService;
import org.titiplex.service.UserService;

@Controller
public class ScenarioController {

    private final LanguageService languageService;
    @Value("${spring.application.name}")
    String appName;

    ScenarioService scenarioService;
    UserService userService;

    public ScenarioController(ScenarioService scenarioService, UserService userService, LanguageService languageService) {
        this.scenarioService = scenarioService;
        this.userService = userService;
        this.languageService = languageService;
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/scenario/create")
    public String createScenario(Model model) {
        model.addAttribute("form", new ScenarioForm("", "", ""));
        model.addAttribute("appName", appName);
        model.addAttribute("langList", languageService.listOptions());
        return "create_scenario";
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/scenario/create")
    public String createScenario(@ModelAttribute("form") ScenarioForm form,
                                 Authentication auth,
                                 Model model) {
        try {
            if (form.languageId() == null || form.languageId().isBlank()) {
                throw new IllegalArgumentException("Language is required");
            }
            if (!languageService.existsById(form.languageId())) {
                throw new IllegalArgumentException("Unknown language id");
            }

            Long userId = userService.getUserByUsername(auth.getName()).getId();

            if (!scenarioService.existsByTitleAndAuthorNameAndLanguageId(form.title(), auth.getName(), form.languageId())) {
                scenarioService.createScenario(form.title(), form.description(), userId, form.languageId());
            }
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("appName", appName);
            model.addAttribute("langList", languageService.listOptions());
            return "create_scenario";
        }
    }

    @GetMapping("/scenario/{id}")
    public String scenarioDetailsPage(Model model, @PathVariable Long id) {
        model.addAttribute("appName", appName);
        model.addAttribute("scenario", scenarioService.getScenario(id));
        return "scenario";
    }


//    @PreAuthorize("hasRole('ADMIN') or @scenarioSecurity.isOwner(#scenarioId, authentication.name)")
//    @PostMapping("/scenario/{scenarioId}/edit")
//    public String editScenario(@PathVariable Long scenarioId) {
//
//    }
}
