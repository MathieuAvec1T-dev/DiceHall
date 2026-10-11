package dicehall.api.Entities.Campaign;

import dicehall.api.Entities.Rule.Rule;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "campaign")
public class Campaign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column
    private Boolean privacy;

    @ManyToMany
    @JoinTable(name = "campaign_rule", joinColumns = @JoinColumn(name = "campaign_id"), inverseJoinColumns = @JoinColumn(name = "rule_id"))
    private Set<Rule> rules;

    @OneToMany(mappedBy = "campaign",cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CampaignUser> campaignUsers;
}
