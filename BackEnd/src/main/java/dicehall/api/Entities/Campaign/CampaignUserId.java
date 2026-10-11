package dicehall.api.Entities.Campaign;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CampaignUserId implements Serializable {
    @Column
    private Integer campaign_id;
    @Column
    private Integer user_id;
}
