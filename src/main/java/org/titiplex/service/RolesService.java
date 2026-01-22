package org.titiplex.service;

import org.springframework.stereotype.Service;
import org.titiplex.persistence.model.Role;
import org.titiplex.persistence.repo.RoleRepository;

@Service
public class RolesService {
    private final RoleRepository roleRepository;

    public RolesService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public void loadRoles() {
        getAdminRole();
        getUserRole();
    }

    public Role getUserRole() {
        return roleRepository.findById("ROLE_USER")
                .orElseGet(() -> roleRepository.save(newRole("ROLE_USER")));
    }

    public Role getAdminRole() {
        return roleRepository.findById("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(newRole("ROLE_ADMIN")));
    }

    private Role newRole(String name) {
        Role r = new Role();
        r.setName(name);
        return r;
    }
}
