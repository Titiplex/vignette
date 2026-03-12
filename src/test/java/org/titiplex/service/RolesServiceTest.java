package org.titiplex.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.titiplex.persistence.model.Role;
import org.titiplex.persistence.repo.RoleRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolesServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RolesService rolesService;

    @Test
    void getUserRole_returnsExistingRoleWhenPresent() {
        Role existing = new Role();
        existing.setName("ROLE_USER");
        when(roleRepository.findById("ROLE_USER")).thenReturn(Optional.of(existing));

        Role result = rolesService.getUserRole();

        assertEquals("ROLE_USER", result.getName());
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void getAdminRole_createsRoleWhenMissing() {
        when(roleRepository.findById("ROLE_ADMIN")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Role result = rolesService.getAdminRole();

        assertEquals("ROLE_ADMIN", result.getName());
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void loadRoles_loadsBothAdminAndUserRoles() {
        when(roleRepository.findById(anyString())).thenReturn(Optional.of(new Role()));

        rolesService.loadRoles();

        verify(roleRepository).findById("ROLE_ADMIN");
        verify(roleRepository).findById("ROLE_USER");
    }
}