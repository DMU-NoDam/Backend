package NoDam.Demo.user.repository;

import NoDam.Demo.user.domain.User;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    @Query("select u from User u where u.oAuthId = :oAuthId and u.oAuthProvider = :oAuthProvider")
    Optional<User> findByOAuthIdAndProvider(@Param("oAuthId") String socialId, @Param("oAuthProvider") String oAuthProvider);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.oAuthId = :oAuthId and u.oAuthProvider = :oAuthProvider")
    Optional<User> findByOAuthIdAndProviderWithLock(@Param("oAuthId") String socialId, @Param("oAuthProvider") String oAuthProvider);

}
