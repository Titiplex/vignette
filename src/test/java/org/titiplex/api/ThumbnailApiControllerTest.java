package org.titiplex.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.titiplex.api.dto.ThumbnailRowDto;
import org.titiplex.api.dto.UploadResponse;
import org.titiplex.persistence.model.Scenario;
import org.titiplex.persistence.model.Thumbnail;
import org.titiplex.persistence.model.User;
import org.titiplex.service.ScenarioService;
import org.titiplex.service.ThumbnailService;
import org.titiplex.service.UserService;
import org.titiplex.service.storage.MediaContent;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("SameParameterValue")
@ExtendWith(MockitoExtension.class)
class ThumbnailApiControllerTest {

    @Mock
    private ThumbnailService thumbnailService;

    @Mock
    private UserService userService;

    @Mock
    private ScenarioService scenarioService;

    @InjectMocks
    private ThumbnailApiController controller;

    @Test
    void list_mapsThumbnailsToRows() {
        Thumbnail t1 = new Thumbnail();
        t1.setId(1L);
        t1.setTitle("Scene 1");
        t1.setIdx(1);

        Thumbnail t2 = new Thumbnail();
        t2.setId(2L);
        t2.setTitle("Scene 2");
        t2.setIdx(2);

        when(thumbnailService.listByScenarioId(9L)).thenReturn(List.of(t1, t2));

        List<ThumbnailRowDto> result = controller.list(9L);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("Scene 1", result.get(0).title());
        assertEquals(2, result.get(1).idx());
    }

    @Test
    void upload_usesAuthenticatedUserAndScenario() throws Exception {
        Authentication auth = auth("alice", "ROLE_USER");

        User user = new User();
        user.setId(12L);
        user.setUsername("alice");

        Scenario scenario = new Scenario();
        scenario.setId(9L);

        Thumbnail saved = new Thumbnail();
        saved.setId(55L);

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "thumb.png",
                "image/png",
                new byte[]{1, 2, 3}
        );

        when(userService.getUserByUsername("alice")).thenReturn(user);
        when(scenarioService.getScenario(9L)).thenReturn(scenario);
        when(thumbnailService.save("Intro", image, scenario, user)).thenReturn(saved);

        UploadResponse response = controller.upload(9L, "Intro", image, auth);

        assertEquals(55L, response.id());
        verify(thumbnailService).save("Intro", image, scenario, user);
    }

    @Test
    void content_buildsResponseEntityWithHeaders() {
        Resource resource = new ByteArrayResource(new byte[]{1, 2, 3, 4});
        MediaContent media = new MediaContent(resource, "image/png", 4L, "\"etag-1\"");

        when(thumbnailService.loadContent(8L)).thenReturn(media);

        ResponseEntity<Resource> response = controller.content(8L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("image/png", Objects.requireNonNull(response.getHeaders().getContentType()).toString());
        assertEquals(4L, response.getHeaders().getContentLength());
        assertEquals("\"etag-1\"", response.getHeaders().getETag());
        assertEquals("public, max-age=3600", response.getHeaders().getFirst("Cache-Control"));
        assertEquals(resource, response.getBody());
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