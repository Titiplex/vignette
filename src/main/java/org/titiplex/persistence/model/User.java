package org.titiplex.persistence.model;

import io.quarkus.security.jpa.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.titiplex.config.CustomPasswordProvider;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "user_")
@UserDefinition
public final class User extends Author {
    @Column(name = "username", unique = true)
    @Username
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Password(value = PasswordType.CUSTOM, provider = CustomPasswordProvider.class)
    @Column(name = "password", nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER) // Roles are often fetched eagerly or lazily depending on needs
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Roles
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private Set<Thumbnail> thumbnails = new HashSet<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private Set<Scenario> scenarios = new HashSet<>();
}
