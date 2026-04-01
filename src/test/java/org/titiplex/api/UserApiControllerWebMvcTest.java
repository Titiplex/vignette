package org.titiplex.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.titiplex.config.SecurityConfig;
import org.titiplex.persistence.model.Role;
import org.titiplex.persistence.model.User;
import org.titiplex.service.UserService;

import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserApiController.class)
@Import(SecurityConfig.class)
class UserApiControllerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService users;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void publicProfile_isAccessibleWithoutAuthentication() throws Exception {
        Role role = new Role();
        role.setName("ROLE_USER");

        User user = new User();
        user.setId(5L);
        user.setUsername("jane");
        user.setDisplayName("Jane");
        user.setBio("Syntax researcher");
        user.setInstitution("Harvard");
        user.setResearchInterests("Syntax");
        user.setProfilePublic(true);
        user.setRoles(Set.of(role));
        user.setAcademyAffiliations(Set.of("LSA"));

        when(users.getExistingUserById(5L)).thenReturn(user);

        mvc.perform(get("/api/users/5/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.username").value("jane"))
                .andExpect(jsonPath("$.displayName").value("Jane"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void updateMyProfile_requiresAuthentication() throws Exception {
        mvc.perform(put("/api/users/me/profile")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "displayName": "Dr Bob",
                                  "bio": "Updated bio",
                                  "institution": "UdeM",
                                  "researchInterests": "Syntax",
                                  "profilePublic": true,
                                  "academyAffiliations": ["Academy A"]
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateMyProfile_requiresCsrfForSessionAuthenticatedRequest() throws Exception {
        mvc.perform(put("/api/users/me/profile")
                        .with(user("bob").roles("USER"))
                        .contentType("application/json")
                        .content("""
                                {
                                  "displayName": "Dr Bob",
                                  "bio": "Updated bio",
                                  "institution": "UdeM",
                                  "researchInterests": "Syntax",
                                  "profilePublic": true,
                                  "academyAffiliations": ["Academy A"]
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateMyProfile_returnsUpdatedProfileWhenAuthenticatedWithCsrf() throws Exception {
        Role role = new Role();
        role.setName("ROLE_USER");

        User current = new User();
        current.setId(2L);
        current.setUsername("bob");

        User updated = new User();
        updated.setId(2L);
        updated.setUsername("bob");
        updated.setEmail("bob@example.com");
        updated.setDisplayName("Dr Bob");
        updated.setBio("Updated bio");
        updated.setInstitution("UdeM");
        updated.setResearchInterests("Syntax");
        updated.setProfilePublic(true);
        updated.setRoles(Set.of(role));
        updated.setAcademyAffiliations(Set.of("Academy A"));

        when(users.getUserByUsername("bob")).thenReturn(current);
        when(users.updateProfile(current, "Dr Bob", "Updated bio", "UdeM", "Syntax", true, Set.of("Academy A")))
                .thenReturn(updated);

        mvc.perform(put("/api/users/me/profile")
                        .with(user("bob").roles("USER"))
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "displayName": "Dr Bob",
                                  "bio": "Updated bio",
                                  "institution": "UdeM",
                                  "researchInterests": "Syntax",
                                  "profilePublic": true,
                                  "academyAffiliations": ["Academy A"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.username").value("bob"))
                .andExpect(jsonPath("$.displayName").value("Dr Bob"))
                .andExpect(jsonPath("$.profilePublic").value(true))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }
}