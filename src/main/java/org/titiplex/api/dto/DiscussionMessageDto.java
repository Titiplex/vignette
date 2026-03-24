package org.titiplex.api.dto;

import org.titiplex.persistence.model.ContributionType;
import org.titiplex.persistence.model.DiscussionTargetType;

import java.time.Instant;

public record DiscussionMessageDto(Long id,
                                   DiscussionTargetType targetType,
                                   String targetId,
                                   Long parentMessageId,
                                   Long authorId,
                                   String authorUsername,
                                   ContributionType contributionType,
                                   String content,
                                   Instant createdAt) {
}