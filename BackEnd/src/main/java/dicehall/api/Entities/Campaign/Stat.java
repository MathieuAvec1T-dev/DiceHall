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
@Table(name = "stat")
public class Stat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 64)
    private String name;

    @Column
    private Boolean required;

    @Column(length = 1024)
    private String selection;

    @Column(length = 64)
    private String defaultValue;

    @Column
    private Integer min;

    @Column
    private Integer max;

    @ManyToMany(mappedBy = "stats")
    private Set<Rule> rules;
}
