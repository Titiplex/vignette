package org.titiplex.persistence.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "accreditation_request")
public class AccreditationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false)
    private Long requestedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccreditationScopeType scopeType;

    @Column
    private Long scenarioId;

    @Column(nullable = false, length = 2000)
    private String motivation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccreditationRequestStatus status = AccreditationRequestStatus.PENDING;

    @Column
    private Long reviewedByUserId;

    @Column
    private Instant reviewedAt;

    @Column(length = 1500)
    private String reviewNote;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "requestedByUserId",
            referencedColumnName = "id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT),
            nullable = false)
    private User requester;
}
