package dicehall.api.Repositories.Rule;

import dicehall.api.Entities.Rule.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagRepository extends JpaRepository<Tag, Integer> {

    @Query("SELECT DISTINCT t FROM Tag t JOIN t.rules r JOIN r.campaigns c WHERE c.id = :campaignID")
    Page<Tag> findByCampaignId(@Param("campaignID") Integer id, Pageable pageable);
}
