package org.titiplex.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.persistence.model.AccreditationRequest;
import org.titiplex.persistence.model.AccreditationRequestStatus;
import org.titiplex.persistence.model.AccreditationScopeType;

import java.util.List;

public interface AccreditationRequestRepository extends JpaRepository<AccreditationRequest, Long> {
    List<AccreditationRequest> findByScopeTypeOrderByCreatedAtDesc(AccreditationScopeType scopeType);

    List<AccreditationRequest> findByScopeTypeAndScenarioIdOrderByCreatedAtDesc(AccreditationScopeType scopeType, Long scenarioId);

    boolean existsByRequestedByUserIdAndScopeTypeAndScenarioIdAndStatus(
            Long requestedByUserId,
            AccreditationScopeType scopeType,
            Long scenarioId,
            AccreditationRequestStatus status
    );
}
