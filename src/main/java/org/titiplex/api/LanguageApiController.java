package org.titiplex.api;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.titiplex.api.dto.LanguageDto;
import org.titiplex.api.dto.LanguageOptionDto;
import org.titiplex.api.dto.LanguageRowDto;
import org.titiplex.api.dto.ScenarioDto;
import org.titiplex.persistence.model.Language;
import org.titiplex.service.LanguageService;
import org.titiplex.service.ScenarioService;

import java.util.List;

@RestController
@RequestMapping("/api/languages")
public class LanguageApiController {

    private final LanguageService languageService;

    public LanguageApiController(LanguageService languageService) {
        this.languageService = languageService;
    }

    /**
     * Retrieves a paginated list of languages with their details.
     *
     * @param page the page number of the data to retrieve, default is 0 if not provided
     * @param size the number of items per page, default is 50 if not provided
     * @return a {@link Page} list of {@link LanguageRowDto} containing information about languages
     */
    @GetMapping
    public Page<LanguageRowDto> list(@RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "50") int size) {
        Page<Language> p = languageService.listLanguages(page, size);
        return p.map(l -> new LanguageRowDto(
                l.getId(),
                l.getName(),
                l.getLevel(),
                l.getFamily() != null ? l.getFamily().getName() : l.getFamilyId(),
                l.getParent() != null ? l.getParent().getName() : l.getParentId()
        ));
    }

    /**
     * Retrieves a language by its unique identifier.
     *
     * @param id ({@link String}) the unique identifier of the language to retrieve
     * @return a {@link LanguageDto} object containing detailed information about the specified language
     */
    @GetMapping("/{id}")
    public LanguageDto getOne(@PathVariable String id) {
        return languageService.getOneDto(id);
    }

    /**
     * Retrieves a paginated list of language options based on a search query.
     *
     * @param q    the search query to filter language options, default is an empty string if not provided
     * @param page the page number of the data to retrieve, default is 0 if not provided
     * @param size the number of items per page, default is 50 if not provided
     * @return a paginated list of {@link  LanguageOptionDto} containing the language options
     */
    @GetMapping("/options")
    public Page<LanguageOptionDto> options(@RequestParam(defaultValue = "") String q,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size) {
        return languageService.searchOptions(q, page, size);
    }

    /**
     * Retrieves a list of scenarios associated with a specific language.
     *
     * @param id ({@link String}) the unique identifier of the language for which scenarios are being retrieved
     * @return a {@link List} of {@link ScenarioDto} objects containing details about the associated scenarios
     */
    @GetMapping("/{id}/scenarios")
    public List<ScenarioDto> getOneScenarios(@PathVariable String id) {
        return languageService.getLanguage(id).getScenarios()
                .stream().map(ScenarioService::toDto).toList();
    }
}
