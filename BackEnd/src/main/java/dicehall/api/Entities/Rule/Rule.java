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

    @ManyToMany(mappedBy = "rules")
    private Set<Campaign> campaigns;

    @ManyToMany
    @JoinTable(name = "rule_tag", joinColumns = @JoinColumn(name = "rule_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags;

    @ManyToMany
    @JsonIgnore
    @JoinTable(name = "rule_stat", joinColumns = @JoinColumn(name = "rule_id"), inverseJoinColumns = @JoinColumn(name = "stat_id"))
    private Set<Stat> stats;


}

