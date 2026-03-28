package org.titiplex.config.components;

import org.springframework.stereotype.Service;
import org.titiplex.service.ThumbnailService;

@Service
public class ThumbnailSecurity {

    private final ThumbnailService thumbnails;

    public ThumbnailSecurity(ThumbnailService thumbnails) {
        this.thumbnails = thumbnails;
    }

    public boolean isOwner(Long thumbnailId, String username) {
        var thumb = thumbnails.getThumbnailById(thumbnailId);
        return
                thumb.getScenario() != null
                        && thumb.getScenario().getAuthor() != null
                        && username.equals(thumb.getScenario().getAuthor().getUsername());
    }
}