package org.titiplex.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.titiplex.persistence.model.Scenario;
import org.titiplex.persistence.model.Thumbnail;
import org.titiplex.persistence.model.User;
import org.titiplex.persistence.repo.ThumbnailRepository;
import org.titiplex.service.storage.FileStorageService;
import org.titiplex.service.storage.StoredFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SuppressWarnings("SequencedCollectionMethodCanBeUsed")
@ExtendWith(MockitoExtension.class)
class ThumbnailServiceTest {
    @Mock
    private ThumbnailRepository thumbnailRepository;
    @Mock
    private FileStorageService storage;
    @Mock
    private MultipartFile image;
    @InjectMocks
    private ThumbnailService thumbnailService;

    @Test
    void save_buildsThumbnailFromInputs() throws Exception {
        Scenario scenario = new Scenario();
        scenario.setId(5L);
        User user = new User();
        user.setId(7L);
        StoredFile stored = new StoredFile("thumbnails/scenario-5/ab/cd/hash.png", "hash", 3L, "image/png", "thumb.png");
        when(storage.storeThumbnail(image, 5L)).thenReturn(stored);
        when(thumbnailRepository.maxIdx(5L)).thenReturn(1);
        when(image.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
        when(thumbnailRepository.save(any(Thumbnail.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Thumbnail result = thumbnailService.save("thumb", image, scenario, user);
        assertEquals("thumb", result.getTitle());
        assertEquals(5L, result.getScenarioId());
        assertEquals(7L, result.getAuthorId());
        assertEquals(2, result.getIdx());
        assertEquals("image/png", result.getContentType());
        assertEquals("hash", result.getImageSha256());
        assertEquals("thumbnails/scenario-5/ab/cd/hash.png", result.getStoragePath());
        assertEquals(3L, result.getSizeBytes());
        assertEquals("thumb.png", result.getOriginalFilename());
    }

    @Test
    void listByScenarioId_delegatesToRepository() {
        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setTitle("t1");
        when(thumbnailRepository.findByScenarioIdOrderByIdxAsc(3L)).thenReturn(List.of(thumbnail));
        List<Thumbnail> result = thumbnailService.listByScenarioId(3L);
        assertEquals(1, result.size());
        assertEquals("t1", result.get(0).getTitle());
    }

    @Test
    void getThumbnailById_returnsExistingEntity() {
        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setId(22L);
        when(thumbnailRepository.findById(22L)).thenReturn(Optional.of(thumbnail));
        Thumbnail result = thumbnailService.getThumbnailById(22L);
        assertEquals(22L, result.getId());
    }
}