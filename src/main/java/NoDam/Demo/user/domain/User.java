package NoDam.Demo.user.domain;

import static NoDam.Demo.util.StringUtil.isEmpty;

import NoDam.Demo.common.domain.BaseEntity;
import NoDam.Demo.common.excetion.CustomException;
import NoDam.Demo.common.excetion.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
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
    private String oAuthProvider;

    @Column
    private String oAuthId;

    public void update(
            String name
    ) {
        if(name != null && !name.isEmpty())
            this.name = name;
    }

    @Builder
    private User(
            String name, UserRole role,
            String oAuthProvider, String oAuthId
    ) {
        if(role == null)
            role = UserRole.VISITOR;

        if (isEmpty(oAuthProvider) && !isEmpty(oAuthId))
            throw new CustomException(ErrorCode.CONFLICT);

        this.name = name;
        this.role = role;
        this.oAuthProvider = oAuthProvider;
        this.oAuthId = oAuthId;
    }

    public boolean isOAuthUser() {
        return oAuthProvider != null && oAuthId != null;
    }

}
