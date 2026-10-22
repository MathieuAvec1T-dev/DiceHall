package dicehall.api.DTOs.Character;

import dicehall.api.DTOs.Campaign.StatDTO;
import dicehall.api.Entities.Character.CharacterStat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CharacterStatDTO {
    public record CharacterStatResponse(StatDTO.StatResponse campaignStat, String value){
        public CharacterStatResponse(CharacterStat characterStat){
            this(new StatDTO.StatResponse(characterStat.getStat()), characterStat.getValue());
        }
    }

    public record CharacterStatRequest(@NotNull Integer statID, @Size(max = 64) String value){}
}
