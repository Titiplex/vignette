package org.titiplex.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.titiplex.api.dto.AudioRowDto;
import org.titiplex.persistence.model.Audio;
import org.titiplex.persistence.model.Scenario;
import org.titiplex.persistence.model.Thumbnail;
import org.titiplex.persistence.repo.AudioRepository;

import java.security.MessageDigest;
import java.util.List;

@Service
public class AudioService {

    private final AudioRepository audios;
    private final ThumbnailService thumbnailService;

    public AudioService(AudioRepository audios, ThumbnailService thumbnailService) {
        this.audios = audios;
        this.thumbnailService = thumbnailService;
    }

    public List<AudioRowDto> listForThumbnail(Long thumbnailId) {
        return audios.findByThumbnailIdOrderByIdxAsc(thumbnailId).stream()
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

        byte[] bytes = audioFile.getBytes();
        if (bytes.length > 10_000_000) { // 10MB MVP
            throw new IllegalArgumentException("audio too large");
        }

        String mime = audioFile.getContentType();
        if (mime == null || !mime.startsWith("audio/")) {
            //  audio/webm or audio/ogg
            mime = "audio/webm";
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

        int effectiveIdx = (idx != null) ? idx : (audios.maxIdx(thumbnailId) + 1);
        if (audios.existsByThumbnailIdAndIdx(thumbnailId, effectiveIdx)) {
            throw new IllegalArgumentException("idx already used for this thumbnail");
        }

        Audio a = new Audio();
        a.setThumbnailId(thumbnailId);
        a.setAudioBytes(bytes);
        a.setMime(mime);
        a.setAudioSha256(sha256Hex(bytes));
        a.setTitle((title == null || title.isBlank()) ? ("Audio " + effectiveIdx) : title.trim());
        a.setIdx(effectiveIdx);
        a.setMarkerX(markerX);
        a.setMarkerY(markerY);
        a.setMarkerLabel((markerLabel == null || markerLabel.isBlank()) ? null : markerLabel.trim());

        Scenario s = t.getScenario();

        a.setAuthorId(authorId);
        a.setScenarioId(t.getScenarioId());
        a.setLanguageId(s.getLanguage_id());

        audios.save(a);
        return a.getId();
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
        audios.deleteById(audioId);
    }

    private static String sha256Hex(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] dig = md.digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : dig) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}