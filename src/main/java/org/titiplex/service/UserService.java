package org.titiplex.service;

import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.titiplex.persistence.model.Role;
import org.titiplex.persistence.model.User;
import org.titiplex.persistence.repo.UserRepository;

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

    public void register(String username, String email, String rawPassword) {
        if (users.existsByUsername(username)) throw new IllegalArgumentException("username taken");
        if (users.existsByEmail(email)) throw new IllegalArgumentException("email taken");

        Role userRole = roles.getUserRole();

                User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.getRoles().add(userRole);

        users.save(u);
    }

    public User getUserById(Long id) {
        User u = new User();
        u.setUsername("User not found");
        return users.findById(id).orElse(u);
    }

    public User getUserByUsername(String username) {
        User u = new User();
        u.setUsername("User not found");
        return users.findByUsername(username).orElse(u);
    }
}
