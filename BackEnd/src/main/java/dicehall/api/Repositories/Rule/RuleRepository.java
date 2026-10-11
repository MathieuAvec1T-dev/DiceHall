package dicehall.api.Repositories.Rule;

import dicehall.api.Entities.Rule.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleRepository extends JpaRepository<Rule, Integer> {
    List<Rule> findByTagsId(Integer id);
}
