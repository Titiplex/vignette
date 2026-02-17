package org.titiplex.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.titiplex.api.dto.AudioRowDto;
import org.titiplex.api.dto.CreateAudioResponse;
import org.titiplex.service.AudioService;
import org.titiplex.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AudioApiController {

    private final AudioService audioService;
    private final UserService userService;

    public AudioApiController(AudioService audioService, UserService userService) {
        this.audioService = audioService;
        this.userService = userService;
    }

    @GetMapping("/thumbnails/{thumbId}/audios")
    public List<AudioRowDto> list(@PathVariable Long thumbId) {
        return audioService.listForThumbnail(thumbId);
    }

    @GetMapping("/audios/{id}/content")
    public ResponseEntity<byte[]> content(@PathVariable Long id) {
        var a = audioService.getAudioOrThrow(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(a.getMime()))
                .header("Cache-Control", "private, max-age=3600")
                .body(a.getAudioBytes());
    }

    @PreAuthorize("hasRole('USER') and @scenarioSecurity.isOwnerByThumbnailId(#thumbId, authentication.name)")
    @PostMapping(value = "/thumbnails/{thumbId}/audios", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CreateAudioResponse upload(
            @PathVariable Long thumbId,
            @RequestParam(defaultValue = "") String title,
            @RequestParam(required = false) Integer idx,
            @RequestPart("audio") MultipartFile audio,
            Authentication auth
    ) throws Exception {

        Long authorId = userService.getUserByUsername(auth.getName()).getId();
        Long id = audioService.createAudio(thumbId, title, idx, authorId, audio);
        return new CreateAudioResponse(id);
    }

    @PreAuthorize("hasRole('USER') and @scenarioSecurity.isOwnerByAudioId(#audioId, authentication.name, @audioService)")
    @DeleteMapping("/audios/{audioId}")
    public void delete(@PathVariable Long audioId) {
        audioService.deleteAudio(audioId);
    }
}