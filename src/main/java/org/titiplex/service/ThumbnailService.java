package org.titiplex.service;

import jakarta.transaction.Transactional;
import org.titiplex.persistence.model.Thumbnail;
import org.titiplex.persistence.repo.ThumbnailRepository;

public class ThumbnailService {
    private final ThumbnailRepository repo;

    public ThumbnailService(ThumbnailRepository thumbnailRepository) {
        this.repo = thumbnailRepository;
    }

    @Transactional
    public Thumbnail save(Thumbnail thumbnail) {
        return repo.save(thumbnail);
    }
}
