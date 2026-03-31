package org.titiplex.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.persistence.model.AccreditationPermissionType;
import org.titiplex.persistence.model.AccreditationRequest;
import org.titiplex.persistence.model.AccreditationRequestStatus;
import org.titiplex.persistence.model.AccreditationScopeType;

import java.util.List;

public interface AccreditationRequestRepository extends JpaRepository<AccreditationRequest, Long> {
    List<AccreditationRequest> findByPermissionTypeAndScopeTypeAndTargetIdOrderByCreatedAtDesc(
            AccreditationPermissionType permissionType,
            AccreditationScopeType scopeType,
            String targetId
    );

    List<AccreditationRequest> findByPermissionTypeAndScopeTypeOrderByCreatedAtDesc(
            AccreditationPermissionType permissionType,
            AccreditationScopeType scopeType
    );

    boolean existsByRequestedByUserIdAndPermissionTypeAndScopeTypeAndTargetIdAndStatus(
            Long requestedByUserId,
            AccreditationPermissionType permissionType,
            AccreditationScopeType scopeType,
            String targetId,
            AccreditationRequestStatus status
    );
}