package org.adt.volunteerscase.entity.rating;

import jakarta.persistence.*;
import lombok.*;
import org.adt.volunteerscase.entity.CoordinatorEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "coordinator_ratings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_coordinator_rating_period_coordinator",
                        columnNames = {"period", "coordinatorId"}
                )
        },
        indexes = {
                @Index(name = "idx_coordinator_rating_period_position", columnList = "period, ratingPosition")
        }
)
public class CoordinatorRatingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coordinatorRatingId")
    private Long coordinatorRatingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RatingPeriod period;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coordinatorId", nullable = false)
    private CoordinatorEntity coordinator;

    @Column(nullable = false)
    private Long totalWeightMinutes;

    @Column(name = "ratingPosition", nullable = false)
    private Integer ratingPosition;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;
}
