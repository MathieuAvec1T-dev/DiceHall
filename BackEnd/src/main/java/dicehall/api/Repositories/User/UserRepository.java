package dicehall.api.Repositories.User;

import dicehall.api.Entities.User.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findAllByUsername(String username, Pageable pageable);
    Optional<User> findByEmail(String email);
    List<User> findByEmailContainingIgnoreCase(String name ,Pageable pageable);

    boolean existsByEmail(String email);
}
