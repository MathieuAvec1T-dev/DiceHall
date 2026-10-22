package dicehall.api.Entities.Character;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dicehall.api.Entities.Campaign.Stat;
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
@Table(name = "character_stat")
public class CharacterStat {
    @EmbeddedId
    private CharacterStatId id;

    @ManyToOne
    @MapsId("owner_id")
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private Character character;

    @ManyToOne
    @MapsId("stat_id")
    @JoinColumn(name = "stat_id")
    private Stat stat;

    @Column(nullable = false, length = 64)
    private String value;
}
