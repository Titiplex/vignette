package org.titiplex.api.dto;

import org.titiplex.persistence.model.ContributionType;
import org.titiplex.persistence.model.DiscussionTargetType;

public record CreateDiscussionMessageRequest(DiscussionTargetType targetType,
                                             String targetId,
                                             Long parentMessageId,
                                             ContributionType contributionType,
                                             String content) {
}