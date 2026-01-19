package org.titiplex.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.persistence.model.Scenario;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    Scenario findByTitle(String title);
}
