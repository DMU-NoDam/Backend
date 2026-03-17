package NoDam.Demo.user.domain;

import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String email;

    @Column
    private String password;

    @Column
    private String name;

    public void updatePassword(String newPassword, PasswordEncoder passwordEncoder) {
        if(passwordEncoder.matches(newPassword, password)) {}
            this.password = newPassword;
    }

    public boolean login(
            PasswordEncoder passwordEncoder,
            String inputPassword
    ) {
        if(email.equals(this.email) && passwordEncoder.matches(inputPassword, this.password))
            return true;

        return false;
    }

    @Builder
    public User(String email, String password, String name) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
    }

}
