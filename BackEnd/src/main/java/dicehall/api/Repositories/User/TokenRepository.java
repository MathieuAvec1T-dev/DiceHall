package dicehall.api.Repositories.User;

import dicehall.api.Entities.User.Token;
import dicehall.api.Entities.User.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, String> {
    Optional<Token> findByUser(User user);
    Optional<Token> findByValue(String value);

    @Transactional
    @Modifying
    @Query("DELETE FROM Token t WHERE t.user = :user")
    void removeTokenByUser(User user);
}
