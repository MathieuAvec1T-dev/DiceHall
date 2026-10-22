package dicehall.api.Repositories.Character;

import dicehall.api.Entities.Character.Character;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterRepository extends JpaRepository<Character, Integer> {
}
