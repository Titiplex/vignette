package org.titiplex.persistence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "thumbnail")
@Getter
@Setter
public class Thumbnail {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "title", nullable = false, unique = true)
    private String title;

    @Column(name = "idx", nullable = false)
    private Integer idx;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "scenario_id", nullable = false)
    private Long scenarioId;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "image_bytes", nullable = false)
    private byte[] imageBytes;

    @Column(name = "image_sha256", nullable = false, unique = true, length = 64)
    private String imageSha256;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "author_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "scenario_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private Scenario scenario;
}