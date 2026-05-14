package org.adt.volunteerscase.entity.rating;

import jakarta.persistence.*;
import lombok.*;
import org.adt.volunteerscase.entity.user.UserEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "user_ratings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_rating_period_user",
                        columnNames = {"period", "userId"}
                )
        },
        indexes = {
                @Index(name = "idx_user_rating_period_position", columnList = "period, ratingPosition")
        }
)
public class UserRatingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userRatingId")
    private Long userRatingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RatingPeriod period;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private Long workedMinutes;

    @Column(name = "ratingPosition", nullable = false)
    private Integer ratingPosition;

    @Column(nullable = false)
    private LocalDateTime calculatedAt;
}