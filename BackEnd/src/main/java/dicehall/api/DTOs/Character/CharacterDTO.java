package dicehall.api.DTOs.Character;

import dicehall.api.DTOs.Campaign.CampaignDTO;
import dicehall.api.Entities.Character.Character;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CharacterDTO {
    public record CharacterResponse(String firstname, String lastname, String nickname, CampaignDTO.CampaignForCharacterResponse campaign, List<CharacterStatDTO.CharacterStatResponse> characterStats, Boolean validate){
        public CharacterResponse(Character character){
            this(character.getFirstname(), character.getLastname(), character.getNickname(), new CampaignDTO.CampaignForCharacterResponse(character.getCampaign()), character.getCharacterStats().stream().map(CharacterStatDTO.CharacterStatResponse::new).toList(), character.getValidate());
        }
    }
    public record CharacterForCampaignResponse(String firstname, String lastname, String nickname, List<CharacterStatDTO.CharacterStatResponse> characterStats, Boolean validate){
        public CharacterForCampaignResponse(Character character){
            this(character.getFirstname(), character.getLastname(), character.getNickname(), character.getCharacterStats().stream().map(CharacterStatDTO.CharacterStatResponse::new).toList(), character.getValidate());
        }
    }

    public record CharacterCreateRequest(@Nullable @Size(max = 64) String firstname, @Nullable @Size(max = 64) String lastname, @Nullable @Size(max = 64) String nickname, @NotNull Integer campaignID, @Nullable Integer userID, @NotNull List<CharacterStatDTO.CharacterStatRequest> characterStats){}
    public record CharacterEditRequest(@NotNull Integer id, @Nullable @Size(max = 64) String firstname, @Nullable @Size(max = 64) String lastName, @Nullable @Size(max = 64) String nickname, @Nullable Integer userID, @NotNull List<CharacterStatDTO.CharacterStatRequest> characterStats){}
    public record CharacterValidateRequest(@NotNull Integer characterID){}
}
