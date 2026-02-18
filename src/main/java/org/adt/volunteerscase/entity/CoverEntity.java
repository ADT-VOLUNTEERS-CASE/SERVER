package org.adt.volunteerscase.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "cover")
public class CoverEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coverId")
    private Integer coverId;

    @NotBlank(message = "link is blank")
    @Column(nullable = false, length = 500)
    private String link;                                      //ссылка на изображение, может быть большой, поэтому длина 500

    @NotNull(message = "width is blank")
    @Min(value = 1, message = "width must be greater than 0")
    @Column(nullable = false)
    private Integer width;                                    //ширина в пикселях, не пустая, больше 0

    @NotNull(message = "height is blank")
    @Min(value = 1, message = "height must be greater than 0")
    @Column(nullable = false)
    private Integer height;                                   //высота в пикселях, не пустая, больше 0
}
