package org.titiplex.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.titiplex.api.dto.ImageUploadForm;
import org.titiplex.persistence.model.Scenario;
import org.titiplex.persistence.model.User;
import org.titiplex.service.ScenarioService;
import org.titiplex.service.ThumbnailService;
import org.titiplex.service.UserService;

@Controller
@RequestMapping("/thumbnail")
public class ThumbnailController {

    private final ThumbnailService thumbnailService;
    private final UserService userService;
    private final ScenarioService scenarioService;

    public ThumbnailController(ThumbnailService thumbnailService, UserService userService, ScenarioService scenarioService) {
        this.thumbnailService = thumbnailService;
        this.userService = userService;
        this.scenarioService = scenarioService;
    }

    @PreAuthorize("hasRole('USER') or @scenarioSecurity.isOwner(#imageForm.scenarioId(), authentication.name)")
    @PostMapping("/upload")
    public String uploadImage(@ModelAttribute("form") ImageUploadForm imageForm, Authentication auth, Model model) {
        try {
            if (imageForm.scenarioId() == null) throw new IllegalArgumentException("scenarioId is null");
            User user = userService.getUserByUsername(auth.getName());
            Scenario scenario = scenarioService.getScenario(imageForm.scenarioId());
            thumbnailService.save(imageForm.title(), imageForm.image(), scenario, user);
            return "redirect:/scenarios/" + scenario.getId();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}
