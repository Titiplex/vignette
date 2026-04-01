package org.titiplex.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.titiplex.api.dto.AudioRowDto;
import org.titiplex.persistence.model.Audio;
import org.titiplex.persistence.model.Scenario;
import org.titiplex.persistence.model.Thumbnail;
import org.titiplex.persistence.repo.AudioRepository;
import org.titiplex.service.storage.FileStorageService;
import org.titiplex.service.storage.MediaContent;
import org.titiplex.service.storage.StoredFile;

import java.util.List;

@Service
public class AudioService {

    private final AudioRepository audios;
    private final ThumbnailService thumbnailService;
    private final FileStorageService storage;

    public AudioService(AudioRepository audios, ThumbnailService thumbnailService, FileStorageService storage) {
        this.audios = audios;
        this.thumbnailService = thumbnailService;
        this.storage = storage;
    }

    public List<AudioRowDto> listForThumbnail(Long thumbnailId) {
        return audios.findByThumbnailIdOrderByIdxAsc(thumbnailId).stream()
                .map(a -> new AudioRowDto(a.getId(), a.getTitle(), a.getIdx(), a.getMime(), a.getMarkerX(), a.getMarkerY(), a.getMarkerLabel()))
                .toList();
    }

    public List<AudioRowDto> listForLanguage(String languageId) {
        return audios.findAllByLanguageId(languageId).stream()
                .map(a -> new AudioRowDto(a.getId(), a.getTitle(), a.getIdx(), a.getMime(), a.getMarkerX(), a.getMarkerY(), a.getMarkerLabel()))
                .toList();
    }

    public Audio getAudioOrThrow(Long audioId) {
        return audios.findById(audioId).orElseThrow();
    }

    @Transactional
    public Long createAudio(Long thumbnailId,
                            String title,
                            Integer idx,
                            Long authorId,
                            MultipartFile audioFile,
                            Double markerX,
                            Double markerY,
                            String markerLabel) throws Exception {

        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException("audio is empty");
        }

        if (markerX != null && (markerX < 0.0 || markerX > 100.0)) {
            throw new IllegalArgumentException("markerX must be between 0 and 100");
        }
        if (markerY != null && (markerY < 0.0 || markerY > 100.0)) {
            throw new IllegalArgumentException("markerY must be between 0 and 100");
        }
        if ((markerX == null) != (markerY == null)) {
            throw new IllegalArgumentException("markerX and markerY must be both set or both empty");
        }

        Thumbnail t = thumbnailService.getThumbnailById(thumbnailId);
        Scenario s = t.getScenario();

        int effectiveIdx = (idx != null) ? idx : (audios.maxIdx(thumbnailId) + 1);
        if (audios.existsByThumbnailIdAndIdx(thumbnailId, effectiveIdx)) {
            throw new IllegalArgumentException("idx already used for this thumbnail");
        }

        StoredFile stored = storage.storeAudio(audioFile, t.getScenarioId(), thumbnailId);

        Audio a = new Audio();
        a.setThumbnailId(thumbnailId);
        a.setMime(stored.contentType());
        a.setAudioSha256(stored.sha256());
        a.setStoragePath(stored.relativePath());
        a.setSizeBytes(stored.sizeBytes());
        a.setOriginalFilename(stored.originalFilename());
        a.setTitle((title == null || title.isBlank()) ? ("Audio " + effectiveIdx) : title.trim());
        a.setIdx(effectiveIdx);
        a.setMarkerX(markerX);
        a.setMarkerY(markerY);
        a.setMarkerLabel((markerLabel == null || markerLabel.isBlank()) ? null : markerLabel.trim());

        a.setAuthorId(authorId);
        a.setScenarioId(t.getScenarioId());
        a.setLanguageId(s.getLanguage_id());

        audios.save(a);
        return a.getId();
    }

    public MediaContent loadContent(Long audioId) {
        Audio a = getAudioOrThrow(audioId);
        return storage.load(a.getStoragePath(), a.getMime(), a.getAudioSha256());
    }

    @Transactional
    public void updateMarker(Long audioId, Double markerX, Double markerY, String markerLabel) {
        if (markerX != null && (markerX < 0.0 || markerX > 100.0)) {
            throw new IllegalArgumentException("markerX must be between 0 and 100");
        }
        if (markerY != null && (markerY < 0.0 || markerY > 100.0)) {
            throw new IllegalArgumentException("markerY must be between 0 and 100");
        }
        if ((markerX == null) != (markerY == null)) {
            throw new IllegalArgumentException("markerX and markerY must be both set or both empty");
        }

        Audio a = getAudioOrThrow(audioId);
        a.setMarkerX(markerX);
        a.setMarkerY(markerY);
        a.setMarkerLabel((markerLabel == null || markerLabel.isBlank()) ? null : markerLabel.trim());
        audios.save(a);
    }

    @Transactional
    public void deleteAudio(Long audioId) {
        Audio a = getAudioOrThrow(audioId);
        storage.deleteQuietly(a.getStoragePath());
        audios.delete(a);
    }
}