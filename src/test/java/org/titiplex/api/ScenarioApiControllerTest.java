package org.titiplex.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.titiplex.api.dto.CreateScenarioRequest;
import org.titiplex.api.dto.CreateScenarioResponse;
import org.titiplex.api.dto.ScenarioDto;
import org.titiplex.persistence.model.Scenario;
import org.titiplex.persistence.model.User;
import org.titiplex.service.LanguageService;
import org.titiplex.service.ScenarioService;
import org.titiplex.service.UserService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("SameParameterValue")
@ExtendWith(MockitoExtension.class)
class ScenarioApiControllerTest {

    @Mock
    private ScenarioService scenarioService;

    @Mock
    private UserService userService;

    @Mock
    private LanguageService languageService;

    @InjectMocks
    private ScenarioApiController controller;

    @Test
    void create_usesAuthenticatedUserAndTrimsTitle() {
        Authentication auth = auth("alice", "ROLE_USER");

        User user = new User();
        user.setId(12L);
        user.setUsername("alice");

        Scenario created = new Scenario();
        created.setId(44L);

        when(languageService.existsById("chuj")).thenReturn(true);
        when(userService.getUserByUsername("alice")).thenReturn(user);
        when(scenarioService.existsByTitleAndAuthorNameAndLanguageId("  Story  ", "alice", "chuj"))
                .thenReturn(false);
        when(scenarioService.createScenario("Story", "A desc", 12L, "chuj"))
                .thenReturn(created);

        CreateScenarioResponse result = controller.create(
                new CreateScenarioRequest("  Story  ", "A desc", "chuj"),
                auth
        );

        assertEquals(44L, result.id());

        verify(scenarioService).createScenario("Story", "A desc", 12L, "chuj");
    }

    @Test
    void getOne_mapsScenarioToDto() {
        User author = new User();
        author.setId(12L);
        author.setUsername("alice");

        Scenario scenario = new Scenario();
        scenario.setId(9L);
        scenario.setTitle("Story");
        scenario.setDescription("Desc");
        scenario.setLanguage_id("chuj");
        scenario.setAuthor(author);
        scenario.setCreatedAt(Instant.parse("2026-03-20T10:15:30Z"));

        when(scenarioService.getScenario(9L)).thenReturn(scenario);

        ScenarioDto dto = controller.getOne(9L);

        assertEquals(9L, dto.id());
        assertEquals("Story", dto.title());
        assertEquals("Desc", dto.description());
        assertEquals("chuj", dto.languageId());
        assertEquals("alice", dto.authorUsername());
    }

    @Test
    void listAll_mapsAllScenariosToDtos() {
        User author = new User();
        author.setId(5L);
        author.setUsername("bob");

        Scenario s1 = new Scenario();
        s1.setId(1L);
        s1.setTitle("First");
        s1.setDescription("D1");
        s1.setLanguage_id("chuj");
        s1.setAuthor(author);
        s1.setCreatedAt(Instant.parse("2026-03-20T10:15:30Z"));

        Scenario s2 = new Scenario();
        s2.setId(2L);
        s2.setTitle("Second");
        s2.setDescription("D2");
        s2.setLanguage_id("kiche");
        s2.setAuthor(author);
        s2.setCreatedAt(Instant.parse("2026-03-21T10:15:30Z"));

        when(scenarioService.listScenarios()).thenReturn(List.of(s1, s2));

        List<ScenarioDto> result = controller.listAll();

        assertEquals(2, result.size());
        assertEquals("First", result.get(0).title());
        assertEquals("Second", result.get(1).title());
    }

    @Test
    void delete_delegatesToService() {
        controller.delete(77L);
        verify(scenarioService).deleteScenario(77L);
    }

    private Authentication auth(String username, String... authorities) {
        return new UsernamePasswordAuthenticationToken(
                username,
                "N/A",
                Arrays.stream(authorities)
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );
    }
}