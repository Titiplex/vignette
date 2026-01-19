package org.titiplex.api;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.titiplex.api.dto.LanguageDto;
import org.titiplex.service.LanguageService;

@RestController
@RequestMapping("/api/language")
public class LanguageRestController {

    private final LanguageService service;

    public LanguageRestController(LanguageService service) {
        this.service = service;
    }

    @GetMapping
    public Page<LanguageDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return service.listLanguagesDto(page, size);
    }

    @GetMapping("/{id}")
    public LanguageDto getOne(@PathVariable String id) {
        return service.getOneDto(id);
    }
}