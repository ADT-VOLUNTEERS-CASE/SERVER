package org.adt.volunteerscase.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.adt.volunteerscase.entity.user.UserEntity;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "coordinators")
public class CoordinatorEntity {

    @Id
    @Column(name = "userId")
    private Integer userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "userId")
    private UserEntity user;

    @Size(max = 255, message = "work location max size is 255")
    @Column(name = "workLocation")
    private String workLocation;
}
