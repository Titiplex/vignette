package org.titiplex.persistence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "scenario")
@Getter
@Setter
public class Scenario {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private String title;

    @Column
    private String description;

    @Column(name = "author_id")
    private Long author_id;

    @Column(name = "language_id")
    private String language_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "author_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "language_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            nullable = false)
    private Language language;

    @OneToMany(mappedBy = "scenario", fetch = FetchType.EAGER)
    private Set<Thumbnail> thumbnails;
}
