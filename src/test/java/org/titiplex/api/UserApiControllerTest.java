package org.titiplex.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.titiplex.api.dto.PublicUserProfileResponse;
import org.titiplex.api.dto.UpdateUserProfileRequest;
import org.titiplex.api.dto.UserProfileResponse;
import org.titiplex.persistence.model.Role;
import org.titiplex.persistence.model.User;
import org.titiplex.service.UserService;

import java.security.Principal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserApiControllerTest {

    @Mock
    private UserService users;

    @InjectMocks
    private UserApiController controller;

    @Test
    void myProfile_mapsPrivateProfile() {
        User user = userWithRole();
        user.setId(1L);
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setDisplayName("Alice");
        user.setBio("Researcher");
        user.setInstitution("UdeM");
        user.setResearchInterests("Morphology");
        user.setProfilePublic(true);
        user.setAcademyAffiliations(Set.of("Team Chuj"));

        when(users.getUserByUsername("alice")).thenReturn(user);

        Principal principal = () -> "alice";

        UserProfileResponse response = controller.myProfile(principal);

        assertEquals(1L, response.id());
        assertEquals("alice", response.username());
        assertEquals("alice@example.com", response.email());
        assertEquals("Alice", response.displayName());
        assertEquals(Set.of("ROLE_USER"), response.roles());
        assertEquals(Set.of("Team Chuj"), response.academyAffiliations());
    }

    @Test
    void updateMyProfile_delegatesAndReturnsUpdatedProfile() {
        User current = userWithRole();
        current.setId(2L);
        current.setUsername("bob");

        User updated = userWithRole();
        updated.setId(2L);
        updated.setUsername("bob");
        updated.setEmail("bob@example.com");
        updated.setDisplayName("Dr Bob");
        updated.setBio("Updated bio");
        updated.setInstitution("UdeM");
        updated.setResearchInterests("Syntax");
        updated.setProfilePublic(true);
        updated.setAcademyAffiliations(Set.of("Academy A"));

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(
                "Dr Bob",
                "Updated bio",
                "UdeM",
                "Syntax",
                true,
                Set.of("Academy A")
        );

        when(users.getUserByUsername("bob")).thenReturn(current);
        when(users.updateProfile(current, "Dr Bob", "Updated bio", "UdeM", "Syntax", true, Set.of("Academy A")))
                .thenReturn(updated);

        Principal principal = () -> "bob";

        UserProfileResponse response = controller.updateMyProfile(request, principal);

        assertEquals(2L, response.id());
        assertEquals("Dr Bob", response.displayName());
        assertEquals("Updated bio", response.bio());
        assertTrue(response.profilePublic());

        verify(users).updateProfile(current, "Dr Bob", "Updated bio", "UdeM", "Syntax", true, Set.of("Academy A"));
    }

    @Test
    void publicProfile_returnsPublicUserProfileWhenVisible() {
        User user = userWithRole();
        user.setId(5L);
        user.setUsername("jane");
        user.setDisplayName("Jane");
        user.setBio("Syntax researcher");
        user.setInstitution("Harvard");
        user.setResearchInterests("Syntax");
        user.setProfilePublic(true);
        user.setAcademyAffiliations(Set.of("LSA"));

        when(users.getExistingUserById(5L)).thenReturn(user);

        PublicUserProfileResponse response = controller.publicProfile(5L);

        assertEquals(5L, response.id());
        assertEquals("jane", response.username());
        assertEquals("Jane", response.displayName());
        assertEquals(Set.of("ROLE_USER"), response.roles());
        assertEquals(Set.of("LSA"), response.academyAffiliations());
    }

    private User userWithRole() {
        Role role = new Role();
        role.setName("ROLE_USER");

        User user = new User();
        user.setRoles(Set.of(role));
        return user;
    }
}