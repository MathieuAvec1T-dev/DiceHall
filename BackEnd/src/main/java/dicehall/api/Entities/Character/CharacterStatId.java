package dicehall.api.Entities.Character;

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
public class CharacterStatId implements Serializable {
    @Column
    private Integer owner_id;
    @Column
    private Integer stat_id;
}
