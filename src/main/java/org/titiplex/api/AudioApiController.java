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

    public record UpdateMarkerRequest(Double markerX, Double markerY, String markerLabel) {
    }

    private final AudioService audioService;
    private final UserService userService;

    public AudioApiController(AudioService audioService, UserService userService) {
        this.audioService = audioService;
        this.userService = userService;
    }

    /**
     * Retrieves a list of audio records associated with a specific thumbnail.
     *
     * @param thumbId ({@link Long}) the ID of the thumbnail for which audio records are to be retrieved
     * @return a {@link List} of {@link AudioRowDto} objects representing the audio records associated with the specified thumbnail
     */
    @GetMapping("/thumbnails/{thumbId}/audios")
    public List<AudioRowDto> list(@PathVariable Long thumbId) {
        return audioService.listForThumbnail(thumbId);
    }

    /**
     * Retrieves the content of an audio file by its ID and returns it as a byte array.
     * The response includes the appropriate MIME type and caching headers.
     *
     * @param id ({@link Long}) the unique identifier of the audio file
     * @return a {@link ResponseEntity} containing the byte array representation of the audio content,
     * the MIME type of the file, and caching headers
     */
    @GetMapping("/audios/{id}/content")
    public ResponseEntity<byte[]> content(@PathVariable Long id) {
        var a = audioService.getAudioOrThrow(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(a.getMime()))
                .header("Cache-Control", "private, max-age=3600")
                .body(a.getAudioBytes());
    }

    /**
     * Uploads a new audio file and associates it with a specific thumbnail.
     * Optionally sets metadata such as title, index, and marker information.
     *
     * @param thumbId     ({@link Long}) the ID of the thumbnail to associate the audio file with
     * @param title       the title of the audio, defaults to an empty string if not provided
     * @param idx         an optional index for ordering or identifying the audio
     * @param markerX     an optional x-coordinate for the marker
     * @param markerY     an optional y-coordinate for the marker
     * @param markerLabel an optional label for the marker, defaults to an empty string if not provided
     * @param audio       ({@link MultipartFile}) the audio file to be uploaded; must be provided in multipart form data
     * @param auth        the authentication information of the current user
     * @return a {@link CreateAudioResponse} object containing the ID of the newly created audio record
     * @throws Exception if any error occurs during the upload process
     */
    @PreAuthorize("hasRole('USER') and @scenarioSecurity.isOwnerByThumbnailId(#thumbId, authentication.name)")
    @PostMapping(value = "/thumbnails/{thumbId}/audios", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CreateAudioResponse upload(
            @PathVariable Long thumbId,
            @RequestParam(defaultValue = "") String title,
            @RequestParam(required = false) Integer idx,
            @RequestParam(required = false) Double markerX,
            @RequestParam(required = false) Double markerY,
            @RequestParam(defaultValue = "") String markerLabel,
            @RequestPart("audio") MultipartFile audio,
            Authentication auth
    ) throws Exception {

        Long authorId = userService.getUserByUsername(auth.getName()).getId();
        Long id = audioService.createAudio(thumbId, title, idx, authorId, audio, markerX, markerY, markerLabel);
        return new CreateAudioResponse(id);
    }

    /**
     * Updates the marker information for a specific audio file.
     *
     * @param audioId ({@link Long}) the ID of the audio file whose marker is being updated
     * @param req     the request body containing the marker information, including:
     *                - {@code markerX}: the x-coordinate of the marker (must be between 0 and 100, or null if not set)
     *                - {@code markerY}: the y-coordinate of the marker (must be between 0 and 100, or null if not set)
     *                - {@code markerLabel}: the label for the marker (can be null or blank if not set)
     */
    @PreAuthorize("hasRole('USER') and @scenarioSecurity.isOwnerByAudioId(#audioId, authentication.name, @audioService)")
    @PatchMapping("/audios/{audioId}/marker")
    public void updateMarker(@PathVariable Long audioId, @RequestBody UpdateMarkerRequest req) {
        audioService.updateMarker(audioId, req.markerX(), req.markerY(), req.markerLabel());
    }

    /**
     * Deletes an audio record identified by the given audio ID.
     * The operation is restricted to users who have the appropriate ownership rights.
     *
     * @param audioId ({@link Long}) the ID of the audio file to be deleted
     */
    @PreAuthorize("hasRole('USER') and @scenarioSecurity.isOwnerByAudioId(#audioId, authentication.name, @audioService)")
    @DeleteMapping("/audios/{audioId}")
    public void delete(@PathVariable Long audioId) {
        audioService.deleteAudio(audioId);
    }
}
