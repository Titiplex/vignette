package org.titiplex.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.titiplex.persistence.model.Role;

public interface RoleRepository extends JpaRepository<Role, String> {
}
