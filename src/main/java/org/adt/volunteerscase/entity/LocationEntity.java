package org.adt.volunteerscase.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "locations")
public class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer locationId;

    @NotBlank(message = "address is blank")
    @Column(nullable = false)
    private String address;                                          //адрес, не пустой

    @Lob
    @Column(name = "additional_notes", columnDefinition = "TEXT")
    private String additionalNotes;                                  //доп заметки, большое поле

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
    @Column(name = "latitude")
    private Double latitude;                                         //широта больше -90 и меньше 90

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
    @Column(name = "longitude")
    private Double longitude;                                        //долгота, больше -180 и меньше 180
}
