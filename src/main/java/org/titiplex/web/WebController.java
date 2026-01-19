package org.titiplex.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.titiplex.persistence.model.Language;
import org.titiplex.service.LanguageService;

@Controller
public class WebController {

    @Value("${spring.application.name}")
    String appName;

    private final LanguageService languageService;

    public WebController(LanguageService languageService) {
        this.languageService = languageService;
    }

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("appName", appName);
        return "home";
    }

    @GetMapping("/languages")
    public String languagesPage(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Language> pageObj = languageService.listLanguages(page, 50);

        int current = pageObj.getNumber();
        int total = pageObj.getTotalPages();
        int window = 2;

        int start = Math.max(0, current - window);
        int end = Math.min(Math.max(total - 1, 0), current + window);

        model.addAttribute("appName", appName);
        model.addAttribute("page", pageObj);
        model.addAttribute("languages", pageObj.getContent());
        model.addAttribute("startPage", start);
        model.addAttribute("endPage", end);

        return "languages";
    }

    @GetMapping("/language/{id}")
    public String languageDetailsPage(Model model, @PathVariable String id) {
        model.addAttribute("appName", appName);
        model.addAttribute("lang", languageService.getLanguage(id));
        return "language";
    }
}