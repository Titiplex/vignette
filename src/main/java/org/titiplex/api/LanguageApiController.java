package org.titiplex.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
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
@Tag(name = "Language objects Endpoint", description = "Endpoints for managing available language.")
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
    @Operation(
            summary = "Lists available languages.",
            description = "Returns a paginated list of languages, including their details."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class, contains = LanguageRowDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid page number or size"
            )
    })
    @GetMapping
    public Page<LanguageRowDto> list(
            @Parameter(
                    description = "Page number to retrieve.",
                    required = true,
                    example = "15",
                    schema = @Schema(type = "integer", minimum = "0")
            )
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(
                    description = "Size of the page to retrieve; i.e. the number of languages to retrieve for a call.",
                    required = true,
                    example = "25",
                    schema = @Schema(type = "integer", minimum = "1", defaultValue = "50")
            )
            @RequestParam(defaultValue = "50") @Min(1) int size
    ) {
        Page<Language> p = languageService.listLanguages(page, size);
    public Page<LanguageRowDto> list(@RequestParam(defaultValue = "") String q,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "50") int size) {
        Page<Language> p = languageService.listLanguages(q, page, size);
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
    @Operation(
            summary = "Retrieves a language by ID.",
            description = "Returns a language object with its details based on the provided ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Language retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LanguageDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Language not found with the specified ID"
            )
    })
    @GetMapping("/{id}")
    public LanguageDto getOne(
            @Parameter(description = "ID of the language to retrieve", required = true)
            @PathVariable String id
    ) {
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
    @Operation(
            summary = "Search for language options.",
            description = "Returns a paginated list of language options based on a search query." +
                    "Useful when needing minimal information to see, especially when querying such as in search motors."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Options retrieved successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class, contains = LanguageOptionDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters"
            )
    })
    @GetMapping("/options")
    public Page<LanguageOptionDto> options(
            @Parameter(
                    description = "The query to search for.",
                    example = "fre",
                    schema = @Schema(type = "string", defaultValue = "\"\"")
            )
            @RequestParam(defaultValue = "") String q,

            @Parameter(
                    description = "The page to return among all the results for the search.",
                    schema = @Schema(type = "integer", minimum = "0", defaultValue = "0")
            )
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(
                    description = "The number of language per page to return.",
                    schema = @Schema(type = "integer", minimum = "1", defaultValue = "50")
            )
            @RequestParam(defaultValue = "50") @Min(1) int size
    ) {
        return languageService.searchOptions(q, page, size);
    }

    /**
     * Retrieves a list of scenarios associated with a specific language.
     *
     * @param id ({@link String}) the unique identifier of the language for which scenarios are being retrieved
     * @return a {@link List} of {@link ScenarioDto} objects containing details about the associated scenarios
     */
    @Operation(
            summary = "Retrieves scenarios associated with a language.",
            description = "Returns a list of scenarios associated with a specific language."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Scenarios successfully retrieved.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ScenarioDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Language not found with the specified ID"
            )
    })
    @GetMapping("/{id}/scenarios")
    public List<ScenarioDto> getOneScenarios(
            @Parameter(description = "ID of the language to retrieve scenarios for", required = true)
            @PathVariable String id
    ) {
        return languageService.getLanguage(id).getScenarios()
                .stream().map(ScenarioService::toDto).toList();
    }
}
