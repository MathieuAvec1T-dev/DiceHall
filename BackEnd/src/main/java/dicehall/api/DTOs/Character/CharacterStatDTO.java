package dicehall.api.DTOs.Character;

import dicehall.api.DTOs.Campaign.StatDTO;
import dicehall.api.Entities.Character.CharacterStat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CharacterStatDTO {
    public record CharacterStatResponse(StatDTO.StatResponse campaignStat, String originalValue, String editedValue){
        public CharacterStatResponse(CharacterStat characterStat){
            this(new StatDTO.StatResponse(characterStat.getStat()), characterStat.getValue(), null);
        }
        public CharacterStatResponse(CharacterStat characterStat, String editedValue){
            this(new StatDTO.StatResponse(characterStat.getStat()), characterStat.getValue(), editedValue);
        }
    }

    //To separate from a request
    public record CharacterStatSimplified(Integer statID, String value, Boolean auto) {
        public CharacterStatSimplified(CharacterStatSimplified characterStat, String editedValue) {
            this(characterStat.statID(), editedValue, true);
        }
    }

    public record CharacterStatRequest(@NotNull Integer statID, @Size(max = 64) String value, @Schema(defaultValue = "true") Boolean auto){
        public CharacterStatRequest  {if(auto == null) auto = true;}
    }
}
