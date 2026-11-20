package dicehall.api.Repositories.Campaign;

import dicehall.api.Entities.Campaign.Stat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StatRepository extends JpaRepository<Stat, Integer> {
    @Query("SELECT DISTINCT s FROM Stat s JOIN s.rules r JOIN r.campaigns c WHERE s.id = :statId AND c.id = :campaignId")
    Optional<Stat> findByStatAndCampaignId(@Param("statId") Integer statId, @Param("campaignId") Integer campaignId);
}
