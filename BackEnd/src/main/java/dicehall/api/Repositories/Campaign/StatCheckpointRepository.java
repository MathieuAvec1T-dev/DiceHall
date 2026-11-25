package dicehall.api.Repositories.Campaign;

import dicehall.api.Entities.Campaign.StatCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatCheckpointRepository extends JpaRepository<StatCheckpoint, Integer> {
}
