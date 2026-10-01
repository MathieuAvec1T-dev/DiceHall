package dicehall.api.Repositories;

import dicehall.api.Entities.User;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findAllByUsername(String username);
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
