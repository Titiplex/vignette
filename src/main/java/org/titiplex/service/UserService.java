package org.titiplex.service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.titiplex.persistence.model.Role;
import org.titiplex.persistence.model.User;
import org.titiplex.persistence.repo.UserRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private final UserRepository users;
    private final RolesService roles;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository, RolesService roleRepository, PasswordEncoder passwordEncoder) {
        this.users = userRepository;
        this.roles = roleRepository;
        this.encoder = passwordEncoder;
    }

    public User register(String username, String email, String rawPassword) {
        if (users.existsByUsername(username)) throw new IllegalArgumentException("username taken");
        if (users.existsByEmail(email)) throw new IllegalArgumentException("email taken");

        Role userRole = roles.getUserRole();

        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setDisplayName(username);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.getRoles().add(userRole);

        return users.save(u);
    }

    public User getUserById(Long id) {
        User u = new User();
        u.setUsername("User not found");
        return users.findById(id).orElse(u);
    }

    public User getUserByUsername(String username) {
        return users.findByUsername(username).orElse(null);
    }

    public User getExistingUserById(Long id) {
        return users.findById(id).orElse(null);
    }

    public User updateProfile(User user,
                              String displayName,
                              String bio,
                              String institution,
                              String researchInterests,
                              Boolean profilePublic,
                              Set<String> academyAffiliations) {
        user.setDisplayName(displayName == null ? null : displayName.trim());
        user.setBio(bio == null ? null : bio.trim());
        user.setInstitution(institution == null ? null : institution.trim());
        user.setResearchInterests(researchInterests == null ? null : researchInterests.trim());
        if (profilePublic != null) user.setProfilePublic(profilePublic);

        if (academyAffiliations != null) {
            var normalized = academyAffiliations.stream()
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.toSet());
            user.setAcademyAffiliations(normalized);
        }

        return users.save(user);
    }

    public boolean existsByUsername(String username) {
        return users.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return users.existsByEmail(email);
    }
}
