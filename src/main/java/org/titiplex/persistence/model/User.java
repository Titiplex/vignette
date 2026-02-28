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

    @Column(nullable = false)
    private boolean profilePublic = false;

    @Column
    private String displayName;

    @Column(length = 1500)
    private String bio;

    @Column
    private String institution;

    @Column(length = 1000)
    private String researchInterests;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_academy_affiliations", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "academy_name")
    private Set<String> academyAffiliations = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_name")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.EAGER)
    private Set<Scenario> scenarios = new HashSet<>();
}
