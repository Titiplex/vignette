package org.titiplex.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.persistence.model.AccreditationScopeType;
import org.titiplex.persistence.model.CommunityAccreditation;

import java.util.List;
import java.util.Optional;

public interface CommunityAccreditationRepository extends JpaRepository<CommunityAccreditation, Long> {
    List<CommunityAccreditation> findByScopeTypeOrderByGrantedAtDesc(AccreditationScopeType scopeType);

    List<CommunityAccreditation> findByScopeTypeAndScenarioIdOrderByGrantedAtDesc(AccreditationScopeType scopeType, Long scenarioId);

    Optional<CommunityAccreditation> findByUserIdAndScopeTypeAndScenarioId(Long userId, AccreditationScopeType scopeType, Long scenarioId);
}
