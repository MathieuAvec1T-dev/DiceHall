package dicehall.api.Repositories.Rule;

import dicehall.api.Entities.Rule.Rule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RuleRepository extends JpaRepository<Rule, Integer> {
    List<Rule> findByTagsId(Integer id);

    @Query("SELECT DISTINCT r FROM Rule r JOIN r.campaigns c WHERE c.id = :campaignId")
    Page<Rule> findByCampaignId(@Param("campaignId") Integer id, Pageable pageable);
}
