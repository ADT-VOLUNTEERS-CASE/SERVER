package org.adt.volunteerscase.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "idx_user_phone", columnList = "phoneNumber"),
                @Index(name = "idx_user_email", columnList = "email")
        })                                                     //ускоряет поиск по этим полям
public class UserEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Integer userId;

    @NotNull(message = "First name is null")
    @Size(max = 100, message = "First name max length is 100")
    @Column(name = "firstname", length = 100, nullable = false)
    private String firstname;                                   //имя, длина <= 100, не null,

    @NotNull(message = "Last name is null")
    @Size(max = 100, message = "Last name max length is 100")
    @Column(name = "lastname", length = 100, nullable = false)
    private String lastname;                                    //фамилия, длина <= 100, не null

    @Size(max = 100, message = "Patronymic max length is 100")
    @Column(name = "patronymic", length = 100)
    private String patronymic;                                  //отчество, длина  <= 100, может не быть

    @NotNull(message = "Phone number is null")
    @Column(unique = true)
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "Phone must be in E.164 format")
    private String phoneNumber;                                 //номер телефона, валидация по E.164

    @NotNull(message = "Email is null")
    @Email(message = "incorrect email format")
    @Size(max = 255, message = "Email max length is 255")
    @Column(name = "email", unique = true, nullable = false)
    private String email;                                       //email, валидация по RFC 5322

    @OneToOne(mappedBy = "userId", cascade = CascadeType.ALL)
    private UserAuthEntity userAuth;                            //связь один к одному с таблицей user_auth, при удалении записи в users удалиться соответствуйщий user_auth

}
