package org.titiplex.persistence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "author")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "surname")
    private String surname;

    @OneToMany(mappedBy = "author", fetch = FetchType.EAGER)
    private Set<Thumbnail> thumbnails = new HashSet<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.EAGER)
    private Set<Audio> audios = new HashSet<>();
}
