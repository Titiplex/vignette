package org.titiplex.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.titiplex.api.dto.*;
import org.titiplex.api.security.PublicOperation;
import org.titiplex.api.security.UserOperation;
import org.titiplex.persistence.model.*;
import org.titiplex.service.CommunityService;
import org.titiplex.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/community")
@Tag(name = "Community", description = "Endpoints for community-related operations.")
public class CommunityApiController {
    private final CommunityService communityService;
    private final UserService userService;

    public CommunityApiController(CommunityService communityService, UserService userService) {
        this.communityService = communityService;
        this.userService = userService;
    }

    @Operation(
            summary = "Lists messages in a discussion.",
            description = "Retrieves all messages in a discussion depending on its type and id."
    )
    @PublicOperation
    @ApiResponses({})
    @GetMapping("/discussions")
    public List<DiscussionMessageDto> listDiscussions(@RequestParam DiscussionTargetType targetType,
                                                      @RequestParam String targetId) {
        return communityService.listMessages(targetType, targetId).stream().map(this::toDto).toList();
    }

    @Operation(
            summary = "Create a discussion.",
            description = """
                    Creates a discussion on a specific topic.
                    
                    The discussion is meant for everyone to see and debate.
                    """
    )
    @UserOperation
    @ApiResponses({})
    @PostMapping("/discussions")
    public DiscussionMessageDto createDiscussion(@RequestBody CreateDiscussionMessageRequest req,
                                                 Authentication auth) {
        Long userId = userService.getUserByUsername(auth.getName()).getId();
        DiscussionMessage created = communityService.createMessage(
                userId,
                req.targetType(),
                req.targetId(),
                req.parentMessageId(),
                req.contributionType(),
                req.content()
        );
        return toDto(created);
    }

    @Operation()
    @UserOperation
    @ApiResponses({})
    @PostMapping("/accreditation-requests")
    public AccreditationRequestDto createAccreditationRequest(@RequestBody CreateAccreditationRequestBody req,
                                                              Authentication auth) {
        Long userId = userService.getUserByUsername(auth.getName()).getId();
        AccreditationRequest created = communityService.createRequest(userId, req.scopeType(), req.scenarioId(), req.motivation());
        return toDto(created);
    }

    @Operation()
    @UserOperation
    @ApiResponses({})
    @GetMapping("/accreditation-requests")
    public List<AccreditationRequestDto> listAccreditationRequests(@RequestParam AccreditationScopeType scopeType,
                                                                   @RequestParam(required = false) Long scenarioId,
                                                                   Authentication auth) {
        if (!canReview(scopeType, scenarioId, auth)) {
            throw forbidden("Not allowed to read these accreditation requests");
        }

        return communityService.listRequests(scopeType, scenarioId).stream().map(this::toDto).toList();
    }

    @Operation()
    @UserOperation
    @ApiResponses({})
    @PostMapping("/accreditation-requests/{requestId}/review")
    public AccreditationRequestDto reviewAccreditationRequest(@PathVariable Long requestId,
                                                              @RequestBody ReviewAccreditationRequestBody req,
                                                              Authentication auth) {
        Long reviewerUserId = userService.getUserByUsername(auth.getName()).getId();
        AccreditationRequest request = communityService.getRequest(requestId);

        if (!canReview(request.getScopeType(), request.getScenarioId(), auth)) {
            throw forbidden("Not allowed to review this request");
        }

        AccreditationRequest reviewed = communityService.reviewRequest(requestId, reviewerUserId, req.approved(), req.reviewNote());
        return toDto(reviewed);
    }

    @Operation()
    @UserOperation
    @ApiResponses({})
    @GetMapping("/accreditations")
    public List<CommunityAccreditationDto> listAccreditations(@RequestParam AccreditationScopeType scopeType,
                                                              @RequestParam(required = false) Long scenarioId,
                                                              Authentication auth) {
        if (!canReview(scopeType, scenarioId, auth)) {
            throw forbidden("Not allowed to list accreditations for this scope");
        }
        return communityService.listAccreditations(scopeType, scenarioId).stream().map(this::toDto).toList();
    }

    @Operation()
    @UserOperation
    @ApiResponses({})
    @PostMapping("/accreditations")
    public CommunityAccreditationDto grantAccreditation(@RequestBody GrantAccreditationBody req,
                                                        Authentication auth) {
        if (!canReview(req.scopeType(), req.scenarioId(), auth)) {
            throw forbidden("Not allowed to grant accreditation for this scope");
        }
        Long granterUserId = userService.getUserByUsername(auth.getName()).getId();
        CommunityAccreditation created = communityService.grantAccreditation(
                req.userId(),
                req.scopeType(),
                req.scenarioId(),
                granterUserId,
                req.note()
        );
        return toDto(created);
    }

    private boolean canReview(AccreditationScopeType scopeType, Long scenarioId, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (scopeType == AccreditationScopeType.GLOBAL) {
            return isAdmin;
        }
        return isAdmin || (scenarioId != null && communityService.isScenarioOwner(scenarioId, auth.getName()));
    }

    private DiscussionMessageDto toDto(DiscussionMessage message) {
        return new DiscussionMessageDto(
                message.getId(),
                message.getTargetType(),
                message.getTargetId(),
                message.getParentMessageId(),
                message.getAuthorId(),
                message.getAuthor() == null ? "Unknown" : message.getAuthor().getUsername(),
                message.getContributionType(),
                message.getContent(),
                message.getCreatedAt()
        );
    }

    private AccreditationRequestDto toDto(AccreditationRequest request) {
        return new AccreditationRequestDto(
                request.getId(),
                request.getRequestedByUserId(),
                request.getRequester() == null ? "Unknown" : request.getRequester().getUsername(),
                request.getScopeType(),
                request.getScenarioId(),
                request.getMotivation(),
                request.getStatus(),
                request.getReviewedByUserId(),
                request.getReviewNote(),
                request.getCreatedAt(),
                request.getReviewedAt()
        );
    }

    private CommunityAccreditationDto toDto(CommunityAccreditation accreditation) {
        return new CommunityAccreditationDto(
                accreditation.getId(),
                accreditation.getUserId(),
                accreditation.getUser() == null ? "Unknown" : accreditation.getUser().getUsername(),
                accreditation.getScopeType(),
                accreditation.getScenarioId(),
                accreditation.getGrantedByUserId(),
                accreditation.getGrantedAt(),
                accreditation.getNote()
        );
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }
}
