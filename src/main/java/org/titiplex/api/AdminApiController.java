package org.titiplex.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.titiplex.api.dto.AdminOverviewDto;
import org.titiplex.api.dto.AdminUserRowDto;
import org.titiplex.api.dto.ApiError;
import org.titiplex.api.dto.ScenarioDto;
import org.titiplex.persistence.model.Role;
import org.titiplex.service.ScenarioService;
import org.titiplex.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Global administration endpoints.")
public class AdminApiController {

    private final UserService users;
    private final ScenarioService scenarios;

    public AdminApiController(UserService users, ScenarioService scenarios) {
        this.users = users;
        this.scenarios = scenarios;
    }

    @Operation(
            summary = "Get admin overview",
            description = "Returns global counters for the administration dashboard."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Overview retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AdminOverviewDto.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Admin privileges required",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/overview")
    public AdminOverviewDto overview(Authentication auth) {
        assertAdmin(auth);

        long userCount = users.countUsers();
        long scenarioCount = scenarios.countAllScenarios();
        long publishedScenarioCount = scenarios.countPublishedScenarios();
        long draftScenarioCount = scenarios.countDraftScenarios();

        return new AdminOverviewDto(
                userCount,
                scenarioCount,
                publishedScenarioCount,
                draftScenarioCount
        );
    }

    @Operation(
            summary = "List users for administration",
            description = "Returns all users for administration purposes."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AdminUserRowDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Admin privileges required",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/users")
    public List<AdminUserRowDto> listUsers(Authentication auth) {
        assertAdmin(auth);

        return users.listAllUsers().stream()
                .map(user -> new AdminUserRowDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getDisplayName(),
                        user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()),
                        user.isProfilePublic()
                ))
                .toList();
    }

    @Operation(
            summary = "List scenarios for administration",
            description = "Returns all scenarios for administration purposes, including drafts."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Scenarios retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ScenarioDto.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Admin privileges required",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/scenarios")
    public List<ScenarioDto> listScenarios(Authentication auth) {
        assertAdmin(auth);

        return scenarios.listAllScenarios().stream()
                .map(scenarios::toDto)
                .toList();
    }

    private void assertAdmin(Authentication auth) {
        boolean isAdmin = auth != null
                && auth.getAuthorities() != null
                && auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

        if (!isAdmin) {
            throw new AccessDeniedException("Admin privileges required");
        }
    }
}