package org.titiplex.config.components;

import org.springframework.stereotype.Service;
import org.titiplex.service.AudioService;
import org.titiplex.service.UserService;

@Service
public class AudioSecurity {
    private final AudioService audios;
    private final UserService users;

    public AudioSecurity(AudioService audios, UserService users) {
        this.audios = audios;
        this.users = users;
    }

    public boolean isOwner(Long audioId, String username) {
        var audio = audios.getAudioOrThrow(audioId);
        var user = users.getUserByUsername(username);
        return user != null && audio.getAuthorId() != null && audio.getAuthorId().equals(user.getId());
    }

}