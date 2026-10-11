package dicehall.api.Entities.Campaign;

import dicehall.api.Entities.User.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "campaign_user")
public class CampaignUser {
    @EmbeddedId
    private CampaignUserId id;

    @ManyToOne
    @MapsId("campaign_id")
    @JoinColumn(name = "campaign_id")
    private Campaign campaign;

    @ManyToOne
    @MapsId("user_id")
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column
    private Role role;
}
