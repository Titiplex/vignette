package org.titiplex.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.persistence.model.Scenario;

import java.util.List;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    Scenario findByTitle(String title);
    boolean existsByIdAndAuthorUsername(Long scenarioId, String username);
    boolean existsByTitleAndAuthorUsernameAndLanguageId(String title, String authorUsername, String languageId);
    List<Scenario> findAllByOrderByCreatedAtDesc();
}
