package dicehall.api.Repositories.Campaign;

import dicehall.api.Entities.Campaign.Campaign;
import dicehall.api.Entities.Campaign.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CampaignRepository extends JpaRepository<Campaign, Integer> {

    List<Campaign> findByRulesTagsId(Integer tagId);

    @Query("SELECT DISTINCT c FROM Campaign c JOIN c.rules r JOIN r.tags t WHERE t.id = :tagId AND c.privacy = false")
    List<Campaign> findByRulesTagsIdAndPublic(@Param("tagId") Integer tagId);

    List<Campaign> findByRulesTagsName(String tagName);

    @Query("SELECT DISTINCT c FROM Campaign c JOIN c.rules r JOIN r.tags t WHERE t.name = :tagName AND c.privacy = false")
    List<Campaign> findByRulesTagsNameAndPublic(@Param("tagName") String tagName);

    @Query("SELECT c FROM Campaign c JOIN c.campaignUsers cu WHERE cu.user.id = :userId")
    List<Campaign> findByUserId(@Param("userId") Integer userId);

    @Query("SELECT c FROM Campaign c JOIN c.campaignUsers cu WHERE cu.user.id = :userId AND cu.role = :role")
    List<Campaign> findByUserIdAndRole(@Param("userId") Integer userId, @Param("role") Role role);

    @Query("SELECT c FROM Campaign c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')) AND (c.privacy = false OR EXISTS (SELECT 1 FROM CampaignUser cu WHERE cu.campaign = c AND cu.id.user_id = :userId))")
    Page<Campaign> findByNameContainingAndCheckIfUserNeeded(@Param("name") String name, @Param("userId") Integer userId, Pageable pageable);
    Page<Campaign> findByNameContainingIgnoreCaseAndPrivacyFalse(String name, Pageable pageable);

}
