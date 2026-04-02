package org.titiplex.config.components;

import org.springframework.stereotype.Service;
import org.titiplex.service.ThumbnailService;
import org.titiplex.service.UserService;

@Service
public class ThumbnailSecurity {
    private final ThumbnailService thumbnails;
    private final UserService users;

    public ThumbnailSecurity(ThumbnailService thumbnails, UserService users) {
        this.thumbnails = thumbnails;
        this.users = users;
    }

    public boolean isOwner(Long thumbnailId, String username) {
        var thumb = thumbnails.getThumbnailById(thumbnailId);
        var user = users.getUserByUsername(username);
        return user != null && thumb.getAuthorId() != null && thumb.getAuthorId().equals(user.getId());
    }
}