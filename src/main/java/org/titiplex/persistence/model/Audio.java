package org.titiplex.persistence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "audio")
public class Audio {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "audio_bytes", nullable = false)
    private byte[] audioBytes;

    @Column(name = "audio_sha256", nullable = false, unique = true, length = 64)
    private String audioSha256;

    @Column(name = "title", nullable = false, unique = true)
    private String title;

    @Column(name = "idx", nullable = false)
    private Integer idx;

    @Column(name="mime", nullable=false, length=64)
    private String mime; //audio/webm

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "scenario_id", nullable = false)
    private Long scenarioId;

    @Column(name = "language_id", nullable = false)
    private String languageId;

    @Column(name = "thumbnail_id", nullable = false)
    private Long thumbnailId;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "thumbnail_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private Thumbnail thumbnail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "language_id",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT)
    )
    private Language language;
}
