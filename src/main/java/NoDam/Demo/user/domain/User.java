package NoDam.Demo.user.domain;

import NoDam.Demo.common.domain.BaseEntity;
import NoDam.Demo.util.StringUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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
    private String name;

    @Enumerated
    private UserRole role = UserRole.VISITOR;

    @Column
    private String email;

    // email user
    @Column
    @Transient
    private String password;

    // social user
    @Column
    private String oAuthProvider;
    @Column
    private String oAuthId;

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

    public static User emailUser(
            String name,
            String email, String password
    ) {
        if(StringUtil.isEmpty(email, password))
            throw new IllegalArgumentException("Email, Password is empty");

        return new User(name, null, email, password, null, null);
    }

    public static User oAuthUser(
            String name, String email,
            String oAuthProvider, String oAuthId
    ) {
        if(StringUtil.isEmpty(oAuthProvider, oAuthId))
            throw new IllegalArgumentException("OAuthId, Provider is empty");

        return new User(name, null, email, null, oAuthProvider, oAuthId);
    }

    private User(
            String name, UserRole role,
            String email, String password,
            String oAuthProvider, String oAuthId
    ) {
        if(StringUtil.isEmpty(oAuthProvider, oAuthId) && StringUtil.isEmpty(email, password))
            throw new IllegalArgumentException();

        if(role == null)
            role = UserRole.VISITOR;

        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.oAuthProvider = oAuthProvider;
        this.oAuthId = oAuthId;
    }

    public boolean isOAuthUser() {
        return oAuthProvider != null && oAuthId != null;
    }

}
