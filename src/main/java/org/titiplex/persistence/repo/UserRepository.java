package org.titiplex.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.persistence.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
