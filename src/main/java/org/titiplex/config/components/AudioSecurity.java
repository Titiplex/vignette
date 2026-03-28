package org.titiplex.config.components;

import org.springframework.stereotype.Service;
import org.titiplex.service.AudioService;

@Service
public class AudioSecurity {

    private final AudioService audios;

    public AudioSecurity(AudioService audios) {
        this.audios = audios;
    }

    public boolean isOwner(Long audioId, String username) {
        var a = audios.getAudioOrThrow(audioId);
        return
                a.getThumbnail() != null
                        && a.getThumbnail().getScenario() != null
                        && a.getThumbnail().getScenario().getAuthor() != null
                        && username.equals(a.getThumbnail().getScenario().getAuthor().getUsername());
    }
}