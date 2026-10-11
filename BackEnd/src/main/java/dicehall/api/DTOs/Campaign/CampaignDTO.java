package dicehall.api.DTOs.Campaign;

import dicehall.api.Entities.Campaign.Campaign;
import dicehall.api.Entities.Rule.Rule;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.stream.Collectors;

public class CampaignDTO {
    public record CampaignResponse(Integer id, @NotBlank @Size(min = 3, max = 64) String name, Boolean privacy, Set<Rule> rules, Set<CampaignUserDTO.CampaignUserCResponse> campaignUsers) {
        public CampaignResponse(Campaign campaign) {
            this(campaign.getId(), campaign.getName(), campaign.getPrivacy(), campaign.getRules(), campaign.getCampaignUsers().stream().map(CampaignUserDTO.CampaignUserCResponse::new).collect(Collectors.toSet()));
        }
    }


    public record CampaignCreateRequest(@NotBlank @Size(min = 3, max = 64) String name, Boolean privacy, @Nullable Set<Integer> ruleIDs) {}
    public record CampaignEditRequest(Integer id, @NotBlank @Size(min = 3, max = 64) String name, Boolean privacy, @Nullable Set<Integer> ruleIDs, @Nullable Set<CampaignUserDTO.CampaignUserEditRequest> campaignUsers) {}
}
