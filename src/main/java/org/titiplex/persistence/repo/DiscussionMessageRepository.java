package org.titiplex.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.persistence.model.DiscussionMessage;
import org.titiplex.persistence.model.DiscussionTargetType;

import java.util.List;

public interface DiscussionMessageRepository extends JpaRepository<DiscussionMessage, Long> {
    List<DiscussionMessage> findByTargetTypeAndTargetIdOrderByCreatedAtAsc(DiscussionTargetType targetType, String targetId);
}
