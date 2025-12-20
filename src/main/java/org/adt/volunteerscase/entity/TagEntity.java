package org.adt.volunteerscase.entity;

import jakarta.persistence.*;
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
@Table(name = "tag",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_tag_name",
                columnNames = "tagName"
        ),
        indexes = @Index(
                name = "idx_tag_name",
                columnList = "tagName"
        ))
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer tagId;

    @NotBlank(message = "tag is blank")
    @Column(name = "tagName", nullable = false)
    private String tagName;
}
