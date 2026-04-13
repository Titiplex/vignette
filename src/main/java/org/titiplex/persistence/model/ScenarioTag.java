package org.titiplex.persistence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(
        name = "scenario_tag",
        uniqueConstraints = @UniqueConstraint(name = "uk_scenario_tag_normalized_name", columnNames = "normalized_name")
)
@Getter
@Setter
public class ScenarioTag {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "normalized_name", nullable = false, length = 64)
    private String normalizedName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToMany(mappedBy = "tags")
    private Set<Scenario> scenarios = new LinkedHashSet<>();
}