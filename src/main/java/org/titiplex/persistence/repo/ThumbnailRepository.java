package org.titiplex.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.persistence.model.Thumbnail;

import java.util.List;

public interface ThumbnailRepository extends JpaRepository<Thumbnail, Long> {
    List<Thumbnail> findByTitle(String title);

    List<Thumbnail> getThumbnailsByScenarioId(Long scenarioId);
}