package dicehall.api.Repositories.Character;

import dicehall.api.Entities.Character.CharacterStat;
import dicehall.api.Entities.Character.CharacterStatId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterStatRepository  extends JpaRepository<CharacterStat, CharacterStatId> {
}
