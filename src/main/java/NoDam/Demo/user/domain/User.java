package NoDam.Demo.user.domain;

import NoDam.Demo.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "user")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SQLDelete(sql = "UPDATE user SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String email;

    @Column
    private String password;

    @Column
    private String name;

    @Enumerated
    private UserRole role = UserRole.VISITOR;

    public void updatePassword(String newPassword) {
        if(newPassword != null && !newPassword.isEmpty())
            this.password = newPassword;
    }

    public void update(
            String name
    ) {
        if(name != null && !name.isEmpty())
            this.name = name;
    }

    private User(String email, String password, String name, UserRole role) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private String email;
        private String password;
        private String name;
        private UserRole role = UserRole.VISITOR; // 기본값

        public UserBuilder email(String email) {
            this.email = email; return this;
        }
        public UserBuilder password(String password) {
            this.password = password; return this;
        }
        public UserBuilder name(String name) {
            this.name = name; return this;
        }
        public UserBuilder role(UserRole role) {
            this.role = role; return this;
        }
        public User build() {
            return new User(email, password, name, role);
        }
    }

}
