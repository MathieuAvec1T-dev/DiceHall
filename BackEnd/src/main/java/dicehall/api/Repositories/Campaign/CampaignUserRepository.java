package dicehall.api.Repositories.Campaign;

import dicehall.api.Entities.Campaign.CampaignUser;
import dicehall.api.Entities.Campaign.CampaignUserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampaignUserRepository extends JpaRepository<CampaignUser, CampaignUserId> {
    Optional<CampaignUser> findByCampaignIdAndUserId(Integer campaignId, Integer userId);
    List<CampaignUser> findByCampaignId(Integer campaignId);
    List<CampaignUser> findByUserId(Integer userId);
}
