package org.adt.volunteerscase.entity.event;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.adt.volunteerscase.entity.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "event",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_event_location_datetime",
                        columnNames = {"locationId", "dateTimestamp"}
                ),
                @UniqueConstraint(
                        name = "uk_event_cover",
                        columnNames = {"coverId"}
                )
        }
)
public class EventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eventId")
    @ToString.Include
    private Integer eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @ToString.Include
    @NotBlank(message = "name is null")
    @Column(nullable = false)
    private String name;

    @Column(length = 5000)
    private String description;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coverId")
    private CoverEntity cover;                                          //ссылка на cover, связь один к одному

    @NotNull(message = "maxCapacity is blank")
    @Min(value = 1, message = "Max capacity must be greater than 0")
    @Column(name = "maxCapacity", nullable = false)
    private Integer maxCapacity;                                        //максимум участников >0


    @NotNull(message = "data is null")
    @Column(name = "dateTimestamp", nullable = false)
    @ToString.Include
    private LocalDateTime dateTimestamp;                                //дата

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locationId", nullable = false)
    private LocationEntity location;                                    //локация, связь многие к одному

    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "event_tags",
            joinColumns = @JoinColumn(name = "eventId"),
            inverseJoinColumns = @JoinColumn(name = "tagId"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_event_tag",
                    columnNames = {"eventId", "tagId"}
            )
    )
    private Set<TagEntity> tags;                                              //тег, связь многие ко многим

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinatorId", nullable = false)
    private CoordinatorEntity coordinator;

    @ToString.Exclude
    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private Set<UserEventEntity> userEvents;

}
