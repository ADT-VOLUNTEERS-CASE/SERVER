package org.adt.volunteerscase.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class UserEventId implements Serializable {

    @Column(name = "userId")
    private Integer userId;

    @Column(name = "eventId")
    private Integer eventId;

}
