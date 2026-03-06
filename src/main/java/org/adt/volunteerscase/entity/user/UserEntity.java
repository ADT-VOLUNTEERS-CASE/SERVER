package org.adt.volunteerscase.entity.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.adt.volunteerscase.entity.TagEntity;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_phone", columnNames = "phoneNumber"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        },
        indexes = {
                @Index(name = "idx_user_phone", columnList = "phoneNumber"),
                @Index(name = "idx_user_email", columnList = "email")
        })                                                     //ускоряет поиск по этим полям
public class UserEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Integer userId;

    @NotBlank(message = "Firstname is null")
    @Size(max = 100, message = "First name max length is 100")
    @Column(name = "firstname", length = 100, nullable = false)
    private String firstname;                                   //имя, длина <= 100, не null,

    @NotBlank(message = "Lastname is null")
    @Size(max = 100, message = "Last name max length is 100")
    @Column(name = "lastname", length = 100, nullable = false)
    private String lastname;                                    //фамилия, длина <= 100, не null

    @Size(max = 100, message = "Patronymic max length is 100")
    @Column(name = "patronymic", length = 100)
    private String patronymic;                                  //отчество, длина <= 100, может не быть

    @NotBlank(message = "Phone number is null")
    @Column(unique = true)
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone must be in E.164 format")
    private String phoneNumber;                                 //номер телефона, валидация по E.164

    @NotBlank(message = "Email is null")
    @Email(message = "incorrect email format")
    @Size(max = 255, message = "Email max length is 255")
    @Column(name = "email", unique = true, nullable = false)
    private String email;                                       //email, валидация по RFC 5322

    @Column(name = "is_admin", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean isAdmin = false;

    @Column(name = "is_coordinator", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private boolean isCoordinator = false;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserAuthEntity userAuth;                            //связь один к одному с таблицей user_auth, при удалении записи в users удалиться соответствующий user_auth

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_tags",
            joinColumns = @JoinColumn(name = "userId"),
            inverseJoinColumns = @JoinColumn(name = "tagId"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_user_tag",
                    columnNames = {"userId", "tagId"}
            )
    )
    private Set<TagEntity> tags;                                     //тег, связь многие ко многим
}
