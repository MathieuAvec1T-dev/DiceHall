package dicehall.api.DTOs.Campaign;

import dicehall.api.DTOs.Character.CharacterDTO;
import dicehall.api.Entities.Campaign.Campaign;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CampaignDTO {
    public record CampaignResponse(Integer id, @NotBlank @Size(min = 3, max = 64) String name, Boolean privacy, Set<CampaignUserDTO.CampaignUserCResponse> campaignUsers, List<CharacterDTO.CharacterForCampaignResponse> characters) {
        public CampaignResponse(Campaign campaign) {
            this(campaign.getId(), campaign.getName(), campaign.getPrivacy(), campaign.getCampaignUsers().stream().map(CampaignUserDTO.CampaignUserCResponse::new).collect(Collectors.toSet()), campaign.getCharacters() == null ? null : campaign.getCharacters().stream().map(CharacterDTO.CharacterForCampaignResponse::new).toList());
        }
    }
    public record CampaignForCharacterResponse(Integer id, @NotBlank @Size(min = 3, max = 64) String name, Boolean privacy, Set<CampaignUserDTO.CampaignUserCResponse> campaignUsers) {
        public CampaignForCharacterResponse(Campaign campaign) {
            this(campaign.getId(), campaign.getName(), campaign.getPrivacy(), campaign.getCampaignUsers().stream().map(CampaignUserDTO.CampaignUserCResponse::new).collect(Collectors.toSet()));
        }
    }

    public record CampaignCreateRequest(@NotBlank @Size(min = 3, max = 64) String name, Boolean privacy, @Nullable Set<Integer> ruleIDs) {}
    public record CampaignEditRequest(Integer id, @NotBlank @Size(min = 3, max = 64) String name, Boolean privacy, @Nullable Set<Integer> ruleIDs, @Nullable Set<CampaignUserDTO.CampaignUserEditRequest> campaignUsers) {}
}
