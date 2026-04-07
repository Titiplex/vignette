package org.titiplex.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.titiplex.api.dto.CreateDiscussionMessageRequest;
import org.titiplex.persistence.model.ContributionType;
import org.titiplex.persistence.model.DiscussionMessage;
import org.titiplex.persistence.model.DiscussionTargetType;
import org.titiplex.persistence.model.User;
import org.titiplex.service.CommunityService;
import org.titiplex.service.UserService;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CommunityApiController.class)
@AutoConfigureMockMvc(addFilters = false)
//@Import(org.titiplex.config.SecurityConfig.class)
class CommunityApiControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommunityService communityService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void listDiscussions_returnsOkAndJson() throws Exception {
        User author = new User();
        author.setId(12L);
        author.setUsername("alice");
        author.setEmail("alice@example.com");
        author.setPasswordHash("hash");
        author.setRoles(Set.of());

        DiscussionMessage message = new DiscussionMessage();
        message.setId(1L);
        message.setTargetType(DiscussionTargetType.LANGUAGE);
        message.setTargetId("chuj");
        message.setParentMessageId(null);
        message.setAuthorId(12L);
        message.setAuthor(author);
        message.setContributionType(ContributionType.GLOSS);
        message.setContent("Interesting gloss");
        message.setCreatedAt(Instant.parse("2026-03-20T10:15:30Z"));

        when(communityService.listMessages(
                DiscussionTargetType.LANGUAGE,
                "chuj"
        )).thenReturn(List.of(message));

        mockMvc.perform(
                        get("/api/community/discussions")
                                .param("targetType", "LANGUAGE")
                                .param("targetId", "chuj")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].targetType").value("LANGUAGE"))
                .andExpect(jsonPath("$[0].targetId").value("chuj"))
                .andExpect(jsonPath("$[0].authorId").value(12))
                .andExpect(jsonPath("$[0].authorUsername").value("alice"))
                .andExpect(jsonPath("$[0].contributionType").value("GLOSS"))
                .andExpect(jsonPath("$[0].content").value("Interesting gloss"));
    }

    @Test
    void createDiscussion_returnsCreatedDto() throws Exception {
        User currentUser = new User();
        currentUser.setId(12L);
        currentUser.setUsername("alice");
        currentUser.setEmail("alice@example.com");
        currentUser.setPasswordHash("hash");
        currentUser.setRoles(Set.of());

        User author = new User();
        author.setId(12L);
        author.setUsername("alice");
        author.setEmail("alice@example.com");
        author.setPasswordHash("hash");
        author.setRoles(Set.of());

        DiscussionMessage created = new DiscussionMessage();
        created.setId(8L);
        created.setTargetType(DiscussionTargetType.LANGUAGE);
        created.setTargetId("chuj");
        created.setParentMessageId(null);
        created.setAuthorId(12L);
        created.setAuthor(author);
        created.setContributionType(ContributionType.GENERAL);
        created.setContent("Hello everyone");
        created.setCreatedAt(Instant.parse("2026-03-20T10:15:30Z"));

        when(userService.getUserByUsername("alice")).thenReturn(currentUser);
        when(communityService.createMessage(
                eq(12L),
                eq(DiscussionTargetType.LANGUAGE),
                eq("chuj"),
                eq(null),
                eq(ContributionType.GENERAL),
                eq("Hello everyone")
        )).thenReturn(created);

        CreateDiscussionMessageRequest body = new CreateDiscussionMessageRequest(
                DiscussionTargetType.LANGUAGE,
                "chuj",
                null,
                ContributionType.GENERAL,
                "Hello everyone"
        );

        mockMvc.perform(
                        post("/api/community/discussions")
                                .with(user("alice").roles("USER"))
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(body))
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.authorId").value(12))
                .andExpect(jsonPath("$.authorUsername").value("alice"))
                .andExpect(jsonPath("$.targetType").value("LANGUAGE"))
                .andExpect(jsonPath("$.targetId").value("chuj"))
                .andExpect(jsonPath("$.contributionType").value("GENERAL"))
                .andExpect(jsonPath("$.content").value("Hello everyone"));
    }
}