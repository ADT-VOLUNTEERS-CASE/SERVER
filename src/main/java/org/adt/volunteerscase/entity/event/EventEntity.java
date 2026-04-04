package org.adt.volunteerscase.entity.event;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.adt.volunteerscase.entity.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
    private Integer eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @NotBlank(message = "name is null")
    @Column(nullable = false)
    private String name;


    @Column(length = 5000)
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coverId")
    private CoverEntity cover;                                          //ссылка на cover, связь один к одному

    @NotNull(message = "maxCapacity is blank")
    @Min(value = 1, message = "Max capacity must be greater than 0")
    @Column(name = "maxCapacity", nullable = false)
    private Integer maxCapacity;                                        //максимум участников >0


    @NotNull(message = "data is null")
    @Column(name = "dateTimestamp", nullable = false)
    private LocalDateTime dateTimestamp;                                //дата

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locationId", nullable = false)
    private LocationEntity location;                                    //локация, связь многие к одному

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordinatorId", nullable = false)
    private CoordinatorEntity coordinator;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private Set<UserEventEntity> userEvents;

}
