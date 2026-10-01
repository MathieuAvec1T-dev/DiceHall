package dicehall.api.Repositories;

import dicehall.api.Entities.Token;
import dicehall.api.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, String> {
    Optional<Token> findByUser(User user);
    Optional<Token> findByValue(String value);

    void removeTokenByUser(User user);
}
