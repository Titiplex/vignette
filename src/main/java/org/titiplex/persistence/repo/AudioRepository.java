package org.titiplex.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.titiplex.persistence.model.Audio;

import java.util.List;

public interface AudioRepository extends JpaRepository<Audio, Long> {
    List<Audio> findByThumbnailIdOrderByIdxAsc(Long thumbnailId);

    boolean existsByThumbnailIdAndIdx(Long thumbnailId, Integer idx);

    @Query("select coalesce(max(a.idx), -1) from Audio a where a.thumbnailId = :thumbId")
    int maxIdx(@Param("thumbId") Long thumbId);
}
