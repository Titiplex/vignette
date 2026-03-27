package org.titiplex.service;

import org.springframework.stereotype.Service;
import org.titiplex.persistence.repo.AccreditationRequestRepository;
import org.titiplex.persistence.repo.CommunityAccreditationRepository;
import org.titiplex.persistence.repo.DiscussionMessageRepository;
import org.titiplex.persistence.model.*;
import org.titiplex.persistence.repo.*;

import java.time.Instant;
import java.util.List;

@Service
public class CommunityService {
    private final DiscussionMessageRepository discussionRepo;
    private final AccreditationRequestRepository requestRepo;
    private final CommunityAccreditationRepository accreditationRepo;
    private final LanguageRepository languageRepository;
    private final AudioRepository audioRepository;
    private final ScenarioRepository scenarioRepository;
    private final UserRepository userRepository;

    public CommunityService(DiscussionMessageRepository discussionRepo,
                            AccreditationRequestRepository requestRepo,
                            CommunityAccreditationRepository accreditationRepo,
                            LanguageRepository languageRepository,
                            AudioRepository audioRepository,
                            ScenarioRepository scenarioRepository,
                            UserRepository userRepository) {
        this.discussionRepo = discussionRepo;
        this.requestRepo = requestRepo;
        this.accreditationRepo = accreditationRepo;
        this.languageRepository = languageRepository;
        this.audioRepository = audioRepository;
        this.scenarioRepository = scenarioRepository;
        this.userRepository = userRepository;
    }

    public List<DiscussionMessage> listMessages(DiscussionTargetType targetType, String targetId) {
        validateTarget(targetType, targetId);
        return discussionRepo.findByTargetTypeAndTargetIdOrderByCreatedAtAsc(targetType, targetId);
    }

    public DiscussionMessage createMessage(Long authorId,
                                           DiscussionTargetType targetType,
                                           String targetId,
                                           Long parentMessageId,
                                           ContributionType contributionType,
                                           String content) {
        if (content == null || content.isBlank()) throw new IllegalArgumentException("Message content is required");
        if (!userRepository.existsById(authorId)) throw new IllegalArgumentException("Unknown user");
        validateTarget(targetType, targetId);

        if (parentMessageId != null) {
            DiscussionMessage parent = discussionRepo.findById(parentMessageId)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown parent message"));

            boolean sameTargetType = parent.getTargetType() == targetType;
            boolean sameTargetId = targetId.equals(parent.getTargetId());

            if (!sameTargetType || !sameTargetId) {
                throw new IllegalArgumentException("Parent message belongs to a different discussion");
            }
        }

        DiscussionMessage message = new DiscussionMessage();
        message.setAuthorId(authorId);
        message.setTargetType(targetType);
        message.setTargetId(targetId);
        message.setParentMessageId(parentMessageId);
        message.setContributionType(contributionType == null ? ContributionType.GENERAL : contributionType);
        message.setContent(content.trim());
        message.setCreatedAt(Instant.now());
        return discussionRepo.save(message);
    }

    public AccreditationRequest createRequest(Long requesterId,
                                              AccreditationScopeType scopeType,
                                              Long scenarioId,
                                              String motivation) {
        if (motivation == null || motivation.isBlank()) throw new IllegalArgumentException("Motivation is required");
        if (!userRepository.existsById(requesterId)) throw new IllegalArgumentException("Unknown user");
        validateScope(scopeType, scenarioId);

        boolean pendingExists = requestRepo.existsByRequestedByUserIdAndScopeTypeAndScenarioIdAndStatus(
                requesterId,
                scopeType,
                scenarioId,
                AccreditationRequestStatus.PENDING
        );
        if (pendingExists) throw new IllegalArgumentException("A pending request already exists");

        AccreditationRequest request = new AccreditationRequest();
        request.setRequestedByUserId(requesterId);
        request.setScopeType(scopeType);
        request.setScenarioId(scopeType == AccreditationScopeType.GLOBAL ? null : scenarioId);
        request.setMotivation(motivation.trim());
        request.setStatus(AccreditationRequestStatus.PENDING);
        request.setCreatedAt(Instant.now());
        return requestRepo.save(request);
    }

    public List<AccreditationRequest> listRequests(AccreditationScopeType scopeType, Long scenarioId) {
        validateScope(scopeType, scenarioId);
        if (scopeType == AccreditationScopeType.GLOBAL) {
            return requestRepo.findByScopeTypeOrderByCreatedAtDesc(scopeType);
        }
        return requestRepo.findByScopeTypeAndScenarioIdOrderByCreatedAtDesc(scopeType, scenarioId);
    }


    public AccreditationRequest getRequest(Long requestId) {
        return requestRepo.findById(requestId).orElseThrow(() -> new IllegalArgumentException("Unknown request"));
    }

    public AccreditationRequest reviewRequest(Long requestId, Long reviewerUserId, boolean approved, String reviewNote) {
        AccreditationRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown request"));

        if (req.getStatus() != AccreditationRequestStatus.PENDING) {
            throw new IllegalArgumentException("Request already reviewed");
        }

        req.setReviewedByUserId(reviewerUserId);
        req.setReviewedAt(Instant.now());
        req.setReviewNote(reviewNote == null ? null : reviewNote.trim());
        req.setStatus(approved ? AccreditationRequestStatus.APPROVED : AccreditationRequestStatus.REJECTED);

        AccreditationRequest saved = requestRepo.save(req);
        if (approved) {
            grantAccreditation(req.getRequestedByUserId(), req.getScopeType(), req.getScenarioId(), reviewerUserId,
                    "Granted from request #" + req.getId());
        }
        return saved;
    }

    public CommunityAccreditation grantAccreditation(Long userId,
                                                     AccreditationScopeType scopeType,
                                                     Long scenarioId,
                                                     Long grantedByUserId,
                                                     String note) {
        validateScope(scopeType, scenarioId);
        if (!userRepository.existsById(userId)) throw new IllegalArgumentException("Unknown user");

        Long normalizedScenarioId = scopeType == AccreditationScopeType.GLOBAL ? null : scenarioId;

        CommunityAccreditation existing = accreditationRepo
                .findByUserIdAndScopeTypeAndScenarioId(userId, scopeType, normalizedScenarioId)
                .orElse(null);
        if (existing != null) return existing;

        CommunityAccreditation grant = new CommunityAccreditation();
        grant.setUserId(userId);
        grant.setScopeType(scopeType);
        grant.setScenarioId(normalizedScenarioId);
        grant.setGrantedByUserId(grantedByUserId);
        grant.setGrantedAt(Instant.now());
        grant.setNote(note == null ? null : note.trim());
        return accreditationRepo.save(grant);
    }

    public List<CommunityAccreditation> listAccreditations(AccreditationScopeType scopeType, Long scenarioId) {
        validateScope(scopeType, scenarioId);
        if (scopeType == AccreditationScopeType.GLOBAL) {
            return accreditationRepo.findByScopeTypeOrderByGrantedAtDesc(scopeType);
        }
        return accreditationRepo.findByScopeTypeAndScenarioIdOrderByGrantedAtDesc(scopeType, scenarioId);
    }

    public boolean isScenarioOwner(Long scenarioId, String username) {
        return scenarioRepository.existsByIdAndAuthorUsername(scenarioId, username);
    }

    private void validateTarget(DiscussionTargetType targetType, String targetId) {
        if (targetType == null) throw new IllegalArgumentException("targetType is required");
        if (targetId == null || targetId.isBlank()) throw new IllegalArgumentException("targetId is required");

        boolean exists = switch (targetType) {
            case LANGUAGE -> languageRepository.existsById(targetId);
            case AUDIO -> {
                long audioId;
                try {
                    audioId = Long.parseLong(targetId);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Audio targetId must be numeric");
                }
                yield audioRepository.existsById(audioId);
            }
        };

        if (!exists) throw new IllegalArgumentException("Unknown target");
    }

    private void validateScope(AccreditationScopeType scopeType, Long scenarioId) {
        if (scopeType == null) throw new IllegalArgumentException("scopeType is required");
        if (scopeType == AccreditationScopeType.SCENARIO) {
            if (scenarioId == null) throw new IllegalArgumentException("scenarioId is required for scenario scope");
            if (!scenarioRepository.existsById(scenarioId)) throw new IllegalArgumentException("Unknown scenario");
        }
    }
}
