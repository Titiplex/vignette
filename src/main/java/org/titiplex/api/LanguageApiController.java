package org.titiplex.api;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.titiplex.api.dto.LanguageDto;
import org.titiplex.api.dto.LanguageOptionDto;
import org.titiplex.api.dto.LanguageRowDto;
import org.titiplex.persistence.model.Language;
import org.titiplex.service.LanguageService;

import java.util.List;

@RestController
@RequestMapping("/api/languages")
public class LanguageApiController {

    private final LanguageService languageService;

    public LanguageApiController(LanguageService languageService) {
        this.languageService = languageService;
    }

    @GetMapping
    public Page<LanguageRowDto> list(@RequestParam(defaultValue="0") int page,
                                     @RequestParam(defaultValue="50") int size) {
        Page<Language> p = languageService.listLanguages(page, size);
        return p.map(l -> new LanguageRowDto(
                l.getId(),
                l.getName(),
                l.getLevel(),
                l.getFamily() != null ? l.getFamily().getName() : l.getFamilyId(),
                l.getParent() != null ? l.getParent().getName() : l.getParentId()
        ));
    }

    @GetMapping("/{id}")
    public LanguageDto getOne(@PathVariable String id) {
        return languageService.getOneDto(id);
    }

    @GetMapping("/options")
    public Page<LanguageOptionDto> options(@RequestParam(defaultValue="") String q,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size) {
        return languageService.searchOptions(q, page, size);
    }
}
