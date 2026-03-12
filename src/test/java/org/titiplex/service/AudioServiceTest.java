package org.titiplex.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.titiplex.api.dto.AudioRowDto;
import org.titiplex.persistence.model.Audio;
import org.titiplex.persistence.model.Scenario;
import org.titiplex.persistence.model.Thumbnail;
import org.titiplex.persistence.repo.AudioRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioServiceTest {

    @Mock
    private AudioRepository audioRepository;
    @Mock
    private ThumbnailService thumbnailService;
    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private AudioService audioService;

    @Test
    void listForThumbnail_mapsRowsInOrder() {
        Audio audio = new Audio();
        audio.setId(7L);
        audio.setTitle("Clip");
        audio.setIdx(2);
        audio.setMime("audio/webm");

        when(audioRepository.findByThumbnailIdOrderByIdxAsc(4L)).thenReturn(List.of(audio));

        List<AudioRowDto> result = audioService.listForThumbnail(4L);

        assertEquals(1, result.size());
        assertEquals("Clip", result.get(0).title());
    }

    @Test
    void createAudio_throwsWhenFileIsMissing() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> audioService.createAudio(1L, "Title", 0, 2L, null, 0.0, 0.0, " "));
        assertEquals("audio is empty", ex.getMessage());
    }

    @Test
    void createAudio_throwsWhenIdxAlreadyUsed() throws Exception {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getBytes()).thenReturn(new byte[]{1, 2});
        when(multipartFile.getContentType()).thenReturn("audio/ogg");
        when(audioRepository.existsByThumbnailIdAndIdx(4L, 5)).thenReturn(true);

        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setScenarioId(9L);
        Scenario scenario = new Scenario();
        scenario.setLanguage_id("fra");
        thumbnail.setScenario(scenario);
        when(thumbnailService.getThumbnailById(4L)).thenReturn(thumbnail);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> audioService.createAudio(4L, "test", 5, 3L, multipartFile, 0.0, 0.0, " "));

        assertEquals("idx already used for this thumbnail", ex.getMessage());
    }

    @Test
    void createAudio_savesAudioAndReturnsId() throws Exception {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(multipartFile.getContentType()).thenReturn("audio/webm");
        when(audioRepository.existsByThumbnailIdAndIdx(6L, 1)).thenReturn(false);

        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setScenarioId(10L);
        Scenario scenario = new Scenario();
        scenario.setLanguage_id("deu");
        thumbnail.setScenario(scenario);
        when(thumbnailService.getThumbnailById(6L)).thenReturn(thumbnail);

        doAnswer(invocation -> {
            Audio saved = invocation.getArgument(0);
            saved.setId(88L);
            return saved;
        }).when(audioRepository).save(any(Audio.class));

        Long id = audioService.createAudio(6L, "  ", 1, 4L, multipartFile, 0.0, 0.0, " ");

        assertEquals(88L, id);
        verify(audioRepository).save(any(Audio.class));
    }

    @Test
    void getAudioOrThrow_returnsAudioWhenFound() {
        Audio audio = new Audio();
        audio.setId(19L);
        when(audioRepository.findById(19L)).thenReturn(Optional.of(audio));

        Audio result = audioService.getAudioOrThrow(19L);

        assertEquals(19L, result.getId());
    }
}