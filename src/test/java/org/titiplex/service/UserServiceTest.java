package org.titiplex.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.titiplex.persistence.model.Role;
import org.titiplex.persistence.model.User;
import org.titiplex.persistence.repo.UserRepository;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RolesService rolesService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void register_throwsWhenUsernameAlreadyTaken() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> userService.register("alice", "alice@mail.com", "pass"));

        assertEquals("username taken", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_savesUserWithEncodedPasswordAndRole() {
        Role role = new Role();
        role.setName("ROLE_USER");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@mail.com")).thenReturn(false);
        when(rolesService.getUserRole()).thenReturn(role);
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.register("alice", "alice@mail.com", "secret");

        assertEquals("alice", result.getUsername());
        assertEquals("alice", result.getDisplayName());
        assertEquals("encoded", result.getPasswordHash());
        assertTrue(result.getRoles().contains(role));
    }

    @Test
    void getUserById_returnsFallbackUserWhenNotFound() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        User result = userService.getUserById(42L);

        assertEquals("User not found", result.getUsername());
    }

    @Test
    void updateProfile_trimsAndFiltersFields() {
        User user = new User();
        user.setProfilePublic(false);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateProfile(
                user,
                "  Display  ",
                "  Bio  ",
                "  Inst  ",
                "  Research  ",
                true,
                Set.of("  Univ A  ", " ", "Lab B")
        );

        assertEquals("Display", result.getDisplayName());
        assertEquals("Bio", result.getBio());
        assertEquals("Inst", result.getInstitution());
        assertEquals("Research", result.getResearchInterests());
        assertTrue(result.isProfilePublic());
        assertEquals(Set.of("Univ A", "Lab B"), result.getAcademyAffiliations());
    }
}