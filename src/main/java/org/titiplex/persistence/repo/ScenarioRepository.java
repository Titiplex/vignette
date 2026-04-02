package org.titiplex.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.titiplex.persistence.model.Scenario;
import org.titiplex.persistence.model.ScenarioVisibilityStatus;

import java.util.List;

public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    Scenario findByTitle(String title);

    boolean existsByIdAndAuthorUsername(Long scenarioId, String username);

    boolean existsByTitleAndAuthorUsernameAndLanguageId(String title, String authorUsername, String languageId);

    List<Scenario> findAllByOrderByCreatedAtDesc();

    List<Scenario> findAllByVisibilityStatusOrderByCreatedAtDesc(ScenarioVisibilityStatus visibilityStatus);

    @Query("""
            select s
            from Scenario s
            where s.visibilityStatus = org.titiplex.persistence.model.ScenarioVisibilityStatus.PUBLISHED
               or lower(s.author.username) = lower(:username)
            order by s.createdAt desc
            """)
    List<Scenario> findVisibleToUsername(@Param("username") String username);
}