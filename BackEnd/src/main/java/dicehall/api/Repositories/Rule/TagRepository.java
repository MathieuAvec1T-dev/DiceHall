package dicehall.api.Repositories.Rule;

import dicehall.api.Entities.Rule.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Integer> {
}
