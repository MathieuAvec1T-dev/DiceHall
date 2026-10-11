package dicehall.api.DTOs.Campaign;

import dicehall.api.Entities.Campaign.CampaignUser;
import dicehall.api.Entities.Campaign.Role;

public class CampaignUserDTO {
    public record CampaignUserCResponse(Integer id, String username, String email, Role role) {
        public CampaignUserCResponse(CampaignUser campaignUser) {
            this(campaignUser.getUser().getId(), campaignUser.getUser().getUsername(), campaignUser.getUser().getEmail(), campaignUser.getRole());
        }
    }

    public record CampaignUserEditRequest(Integer userID, Role role) {}
}
