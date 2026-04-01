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
import org.titiplex.api.dto.AudioRowDto;
import org.titiplex.api.dto.CreateAudioResponse;
import org.titiplex.api.dto.UpdateMarkerRequest;
import org.titiplex.persistence.model.User;
import org.titiplex.service.AudioService;
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
class AudioApiControllerTest {

    @Mock
    private AudioService audioService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AudioApiController controller;

    @Test
    void list_returnsServiceDtos() {
        when(audioService.listForThumbnail(7L)).thenReturn(List.of(
                new AudioRowDto(1L, "Audio 1", 1, "audio/webm", 10.0, 20.0, "A"),
                new AudioRowDto(2L, "Audio 2", 2, "audio/webm", null, null, null)
        ));

        List<AudioRowDto> result = controller.list(7L);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("Audio 1", result.get(0).title());
        assertEquals("audio/webm", result.get(0).mime());
    }

    @Test
    void content_buildsResponseEntityWithHeaders() {
        Resource resource = new ByteArrayResource(new byte[]{9, 8, 7});
        MediaContent media = new MediaContent(resource, "audio/webm", 3L, "\"audio-etag\"");

        when(audioService.loadContent(4L)).thenReturn(media);

        ResponseEntity<Resource> response = controller.content(4L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("audio/webm", Objects.requireNonNull(response.getHeaders().getContentType()).toString());
        assertEquals(3L, response.getHeaders().getContentLength());
        assertEquals("\"audio-etag\"", response.getHeaders().getETag());
        assertEquals("private, max-age=3600", response.getHeaders().getFirst("Cache-Control"));
        assertEquals(resource, response.getBody());
    }

    @Test
    void upload_usesAuthenticatedUserAndDelegatesToService() throws Exception {
        Authentication auth = auth("alice", "ROLE_USER");

        User user = new User();
        user.setId(12L);
        user.setUsername("alice");

        MockMultipartFile audio = new MockMultipartFile(
                "audio",
                "clip.webm",
                "audio/webm",
                new byte[]{1, 2, 3}
        );

        when(userService.getUserByUsername("alice")).thenReturn(user);
        when(audioService.createAudio(9L, "Greeting", 2, 12L, audio, 10.0, 20.0, "speaker"))
                .thenReturn(42L);

        CreateAudioResponse response = controller.upload(
                9L,
                "Greeting",
                2,
                10.0,
                20.0,
                "speaker",
                audio,
                auth
        );

        assertEquals(42L, response.id());

        verify(audioService).createAudio(9L, "Greeting", 2, 12L, audio, 10.0, 20.0, "speaker");
    }

    @Test
    void updateMarker_delegatesToService() {
        controller.updateMarker(5L, new UpdateMarkerRequest(11.0, 22.0, "target"));

        verify(audioService).updateMarker(5L, 11.0, 22.0, "target");
    }

    @Test
    void delete_delegatesToService() {
        controller.delete(6L);
        verify(audioService).deleteAudio(6L);
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