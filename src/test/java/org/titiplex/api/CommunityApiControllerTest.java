package org.titiplex.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.titiplex.api.dto.CreateDiscussionMessageRequest;
import org.titiplex.api.dto.DiscussionMessageDto;
import org.titiplex.api.dto.GrantAccreditationBody;
import org.titiplex.persistence.model.*;
import org.titiplex.service.CommunityService;
import org.titiplex.service.UserService;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityApiControllerTest {

    @Mock
    private CommunityService communityService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CommunityApiController controller;

    @Test
    void listDiscussions_mapsDiscussionMessagesToDtos() {
        User author = new User();
        author.setId(12L);
        author.setUsername("alice");

        DiscussionMessage message = new DiscussionMessage();
        message.setId(1L);
        message.setTargetType(DiscussionTargetType.LANGUAGE);
        message.setTargetId("chuj");
        message.setParentMessageId(null);
        message.setAuthorId(12L);
        message.setAuthor(author);
        message.setContributionType(ContributionType.GLOSS);
        message.setContent("Interesting glossing issue");
        message.setCreatedAt(Instant.parse("2026-03-20T10:15:30Z"));

        when(communityService.listMessages(DiscussionTargetType.LANGUAGE, "chuj"))
                .thenReturn(List.of(message));

        List<DiscussionMessageDto> result = controller.listDiscussions(
                DiscussionTargetType.LANGUAGE,
                "chuj"
        );

        assertEquals(1, result.size());
        DiscussionMessageDto dto = result.getFirst();
        assertEquals(1L, dto.id());
        assertEquals("alice", dto.authorUsername());
        assertEquals(ContributionType.GLOSS, dto.contributionType());
        assertEquals("Interesting glossing issue", dto.content());
    }

    @Test
    void createDiscussion_usesAuthenticatedUserAndDelegatesToService() {
        Authentication auth = auth("alice", "ROLE_USER");

        User currentUser = new User();
        currentUser.setId(12L);
        currentUser.setUsername("alice");

        User author = new User();
        author.setId(12L);
        author.setUsername("alice");

        DiscussionMessage created = new DiscussionMessage();
        created.setId(8L);
        created.setTargetType(DiscussionTargetType.LANGUAGE);
        created.setTargetId("chuj");
        created.setAuthorId(12L);
        created.setAuthor(author);
        created.setContributionType(ContributionType.GENERAL);
        created.setContent("Hello");
        created.setCreatedAt(Instant.parse("2026-03-20T10:15:30Z"));

        when(userService.getUserByUsername("alice")).thenReturn(currentUser);
        when(communityService.createMessage(
                12L,
                DiscussionTargetType.LANGUAGE,
                "chuj",
                null,
                null,
                "Hello"
        )).thenReturn(created);

        DiscussionMessageDto dto = controller.createDiscussion(
                new CreateDiscussionMessageRequest(
                        DiscussionTargetType.LANGUAGE,
                        "chuj",
                        null,
                        null,
                        "Hello"
                ),
                auth
        );

        assertEquals(8L, dto.id());
        assertEquals(12L, dto.authorId());
        assertEquals("alice", dto.authorUsername());

        verify(communityService).createMessage(
                12L,
                DiscussionTargetType.LANGUAGE,
                "chuj",
                null,
                null,
                "Hello"
        );
    }

    @Test
    void listAccreditationRequests_allowsScenarioOwner() {
        Authentication auth = auth("owner", "ROLE_USER");

        User requester = new User();
        requester.setId(17L);
        requester.setUsername("bob");

        AccreditationRequest request = new AccreditationRequest();
        request.setId(4L);
        request.setRequestedByUserId(17L);
        request.setRequester(requester);
        request.setScopeType(AccreditationScopeType.SCENARIO);
        request.setScenarioId(9L);
        request.setMotivation("I can help");
        request.setStatus(AccreditationRequestStatus.PENDING);
        request.setCreatedAt(Instant.parse("2026-03-21T09:00:00Z"));

        when(communityService.isScenarioOwner(9L, "owner")).thenReturn(true);
        when(communityService.listRequests(AccreditationScopeType.SCENARIO, 9L))
                .thenReturn(List.of(request));

        var result = controller.listAccreditationRequests(
                AccreditationScopeType.SCENARIO,
                9L,
                auth
        );

        assertEquals(1, result.size());
        assertEquals("bob", result.getFirst().requestedByUsername());
        assertEquals("I can help", result.getFirst().motivation());
    }

    @Test
    void grantAccreditation_allowsAdminForGlobalScope() {
        Authentication auth = auth("root", "ROLE_ADMIN");

        User granter = new User();
        granter.setId(1L);
        granter.setUsername("root");

        User accreditedUser = new User();
        accreditedUser.setId(7L);
        accreditedUser.setUsername("bob");

        CommunityAccreditation created = new CommunityAccreditation();
        created.setId(22L);
        created.setUserId(7L);
        created.setUser(accreditedUser);
        created.setScopeType(AccreditationScopeType.GLOBAL);
        created.setScenarioId(null);
        created.setGrantedByUserId(1L);
        created.setGrantedAt(Instant.parse("2026-03-22T08:00:00Z"));
        created.setNote("Granted");

        when(userService.getUserByUsername("root")).thenReturn(granter);
        when(communityService.grantAccreditation(
                7L,
                AccreditationScopeType.GLOBAL,
                null,
                1L,
                "Granted"
        )).thenReturn(created);

        var result = controller.grantAccreditation(
                new GrantAccreditationBody(
                        7L,
                        AccreditationScopeType.GLOBAL,
                        null,
                        "Granted"
                ),
                auth
        );

        assertEquals(22L, result.id());
        assertEquals("bob", result.username());
        assertEquals(AccreditationScopeType.GLOBAL, result.scopeType());

        verify(communityService).grantAccreditation(
                7L,
                AccreditationScopeType.GLOBAL,
                null,
                1L,
                "Granted"
        );
    }

    private Authentication auth(String username, String... authorities) {
        return new UsernamePasswordAuthenticationToken(
                username,
                "N/A",
                Arrays.stream(authorities)
                        .map(SimpleGrantedAuthority::new)
                        .toList()
        );
    }
}