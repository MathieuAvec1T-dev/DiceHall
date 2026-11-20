package dicehall.api.Entities.Rule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dicehall.api.Entities.Campaign.Campaign;
import dicehall.api.Entities.Campaign.Stat;
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
@Table(name = "rule")
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(nullable = false, length = 1024)
    private String description;

    @Column(nullable = false, length = 244)
    private String operation;

    @Column(nullable = false)
    private Integer priority;

    @ManyToMany(mappedBy = "rules")
    private Set<Campaign> campaigns;

    @ManyToMany
    @JoinTable(name = "rule_tag", joinColumns = @JoinColumn(name = "rule_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags;

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "rule_stat", joinColumns = @JoinColumn(name = "rule_id"), inverseJoinColumns = @JoinColumn(name = "stat_id"))
    private Set<Stat> stats;

    //Copy method to keep a Rule in a Hashmap without causing problem with hibernate.
    public Rule copy(){
        Rule newRule = new Rule();
        newRule.setName(name);
        newRule.setDescription(description);
        newRule.setOperation(operation);
        newRule.setCampaigns(campaigns);
        newRule.setTags(tags);
        newRule.setStats(stats);
        newRule.setPriority(priority);
        return newRule;
    }

    //Copy method to keep a Rule in a Hashmap without causing problem with hibernate.
    public Rule copyWithId(){
        Rule newRule = copy();
        newRule.setId(id);
        return newRule;
    }
}

