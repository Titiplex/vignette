package org.titiplex.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.persistence.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
