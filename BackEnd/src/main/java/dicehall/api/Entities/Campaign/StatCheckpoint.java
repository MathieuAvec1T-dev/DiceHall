package dicehall.api.Entities.Campaign;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stat_checkpoint")
public class StatCheckpoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "value")
    private Integer value;

    @Column(nullable = false)
    private Boolean superior;

    @Setter(AccessLevel.NONE)
    @Generated(event = { EventType.INSERT, EventType.UPDATE })
    @Column(name = "toSuccess", insertable = false, updatable = false)
    private Boolean toSuccess;

    @Column(length = 64)
    private String name;

    @ManyToMany
    @JoinTable(name = "stat_stat_checkpoint", joinColumns = @JoinColumn(name = "stat_checkpoint_id"), inverseJoinColumns = @JoinColumn(name = "stat_id"))
    private Set<Stat> stats;
}
