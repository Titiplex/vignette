package org.titiplex.persistence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "user_")
public final class User extends Author {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_name")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.EAGER)
    private Set<Thumbnail> thumbnails = new HashSet<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.EAGER)
    private Set<Scenario> scenarios = new HashSet<>();
}
