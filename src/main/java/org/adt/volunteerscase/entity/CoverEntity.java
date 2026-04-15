package org.adt.volunteerscase.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "files")
public class CoverEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fileId")
    private Integer coverId;

    @NotBlank(message = "link is blank")
    @Column(nullable = false, unique = true, length = 1024)
    private String link;

    @NotBlank(message = "metadata is blank")
    @Column(nullable = false, length = 4000)
    private String metadata;

    @NotNull(message = "createdAt is blank")
    @Column(name = "createdAt", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "deletedAt")
    private Long deletedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now().toEpochMilli();
        }
    }
}
