package org.titiplex.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.persistence.model.Audio;

public interface AudioRepository extends JpaRepository<Audio, Long> {
}
