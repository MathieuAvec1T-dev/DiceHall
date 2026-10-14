package dicehall.api.Services.Campaign;

import dicehall.api.DTOs.Campaign.CampaignDTO;
import dicehall.api.DTOs.Campaign.CampaignUserDTO;
import dicehall.api.Entities.Campaign.Campaign;
import dicehall.api.Entities.Campaign.CampaignUser;
import dicehall.api.Entities.Campaign.CampaignUserId;
import dicehall.api.Entities.Campaign.Role;
import dicehall.api.Entities.Rule.Rule;
import dicehall.api.Entities.User.Token;
import dicehall.api.Entities.User.User;
import dicehall.api.Repositories.Campaign.CampaignRepository;
import dicehall.api.Repositories.Campaign.CampaignUserRepository;
import dicehall.api.Repositories.Rule.RuleRepository;
import dicehall.api.Repositories.User.UserRepository;
import dicehall.api.Security.Exceptions.ResourceNotFoundException;
import dicehall.api.Services.User.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CampaignService {

    private final UserService userService;
    private final RuleRepository ruleRepository;
    private final CampaignRepository campaignRepository;
    private final CampaignUserRepository campaignUserRepository;
    private final UserRepository userRepository;

    public CampaignService(UserService userService, RuleRepository ruleRepository, CampaignRepository campaignRepository, CampaignUserRepository campaignUserRepository, UserRepository userRepository) {
        this.userService = userService;
        this.ruleRepository = ruleRepository;
        this.campaignRepository = campaignRepository;
        this.campaignUserRepository = campaignUserRepository;
        this.userRepository = userRepository;
    }

    /**
     * Find {@link Campaign Campaigns} where the name contains the search value.
     * @param searchParam The String you want to search for in the campaigns name.
     * @param pageable The {@link Pageable} parameters for pagination.
     * @param privacy {@link Boolean} if you want to add private {@link Campaign Campaigns} or not.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return a {@link List<CampaignDTO.CampaignResponse> List of Campaigns} found.
     */
    public List<CampaignDTO.CampaignResponse> findIfNameContains(String searchParam, Pageable pageable, Boolean privacy, String authorization) {
        List<Campaign> campaigns;
        if(privacy){
            if(authorization==null) throw new BadCredentialsException("Invalid token for this campaign: " + "null");
            User user = userService.validate(authorization);
            campaigns = campaignRepository.findByNameContainingAndCheckIfUserNeeded(searchParam, user.getId(), pageable).getContent();
        } else {
            campaigns = campaignRepository.findByNameContainingIgnoreCaseAndPrivacyFalse(searchParam, pageable).getContent();
        }
        return campaigns.stream().map(CampaignDTO.CampaignResponse::new).toList();
    }

    /**
     * Find a {@link Campaign Campaign} by ID.
     * @param id of the {@link Campaign}.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return the found {@link CampaignDTO.CampaignResponse Campaign}.
     */
    public CampaignDTO.CampaignResponse findById(Integer id , String authorization) {
        Campaign campaign = campaignRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Campaign", id.toString()));
        if(campaign.getPrivacy()) {
            if(authorization==null) throw new BadCredentialsException("Invalid token for this campaign: " + "null");
            User user = userService.validate(authorization);
            if(campaign.getCampaignUsers().stream().noneMatch(campaignUser -> Objects.equals(campaignUser.getId().getUser_id(), user.getId()))) throw new BadCredentialsException("Invalid token for this campaign: " + authorization);
        }
        return new CampaignDTO.CampaignResponse(campaign);
    }

    /**
     * Find {@link Campaign Campaigns} that a {@link User} own.
     * @param id of the {@link User Owner}.
     * @param privacy {@link Boolean} if you want to add private {@link Campaign Campaigns} or not.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return a {@link List<CampaignDTO.CampaignResponse> List of Campaigns} found.
     */
    public List<CampaignDTO.CampaignResponse> findByOwnerId(Integer id, boolean privacy, String authorization) {
        List<Campaign> campaigns = campaignRepository.findByUserIdAndRole(id, Role.Owner);
        if(privacy){
            if(authorization==null) throw new BadCredentialsException("Invalid token for this campaign: " + "null");
            User user = userService.validate(authorization);
            campaigns.removeIf(campaign -> campaign.getCampaignUsers().stream().noneMatch(campaignUser -> Objects.equals(campaignUser.getId().getUser_id(), user.getId())));
        } else {
            campaigns.removeIf(campaign -> campaign.getPrivacy() || campaign.getCampaignUsers().stream().noneMatch(campaignUser -> Objects.equals(campaignUser.getId().getUser_id(), id)));
        }
        return campaigns.stream().map(CampaignDTO.CampaignResponse::new).toList();
    }

    /**
     * Find {@link Campaign Campaigns} that a {@link User} is part of.
     * @param id of the {@link User}.
     * @param privacy {@link Boolean} if you want to add private {@link Campaign Campaigns} or not.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return a {@link List<CampaignDTO.CampaignResponse> List of Campaigns} found.
     */
    public List<CampaignDTO.CampaignResponse> findByUserId(Integer id, boolean privacy, String authorization) {
        List<Campaign> campaigns = campaignRepository.findByUserId(id);
        if(privacy){
            if(authorization==null) throw new BadCredentialsException("Invalid token for this campaign: " + "null");
            User user = userService.validate(authorization);
            campaigns.removeIf(campaign -> campaign.getPrivacy() && campaign.getCampaignUsers().stream().noneMatch(campaignUser -> Objects.equals(campaignUser.getId().getUser_id(), user.getId())));
        } else {
            campaigns.removeIf(Campaign::getPrivacy);
        }
        return campaigns.stream().map(CampaignDTO.CampaignResponse::new).toList();
    }

    /**
     * Create a {@link Campaign}.
     * @param campaignRequest {@link CampaignDTO.CampaignCreateRequest} with campaign data.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return The {@link CampaignDTO.CampaignResponse Campaign} that was created.
     */
    public CampaignDTO.CampaignResponse createCampaign(CampaignDTO.CampaignCreateRequest campaignRequest, String authorization) {
        User user = userService.validate(authorization);
        Campaign campaign = new Campaign();
        campaign.setName(campaignRequest.name());
        campaign.setPrivacy(campaignRequest.privacy());
        if(campaignRequest.ruleIDs() != null && !campaignRequest.ruleIDs().isEmpty()){
            List<Rule> rules = ruleRepository.findAllById(campaignRequest.ruleIDs());
            if(rules.size() != campaignRequest.ruleIDs().size()) throw new IllegalStateException("One or more rule IDs do not exist.");
            campaign.setRules(new HashSet<>(rules));
        } else campaign.setRules(new HashSet<>());
        campaign = campaignRepository.save(campaign);
        try{
            Set<CampaignUser> campaignUsers = new HashSet<>();
            campaignUsers.add(new CampaignUser(new CampaignUserId(campaign.getId(), user.getId()), campaign, user, Role.Owner));
            campaign.setCampaignUsers(campaignUsers);
            campaign = campaignRepository.save(campaign);
            if(user.getActiveCampaign() == null) {
                user.setActiveCampaign(campaign);
                userRepository.save(user);
            }
        } catch(Exception e){
            campaignRepository.deleteById(campaign.getId());
            throw e;
        }
        return new CampaignDTO.CampaignResponse(campaign);
    }

    /**
     * Edit the campaign.
     * Only an Owner can edit admins and players.
     * An Admin can only edit players.
     * @param campaignRequest {@link CampaignUserDTO.CampaignUserEditRequest} with new data.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return The {@link CampaignDTO.CampaignResponse Campaign} that was edited.
     */
    public CampaignDTO.CampaignResponse editCampaign(CampaignDTO.CampaignEditRequest campaignRequest, String authorization) {
        User user = userService.validate(authorization);
        Campaign campaign = campaignRepository.findById(campaignRequest.id()).orElseThrow(() -> new ResourceNotFoundException("Campaign", campaignRequest.id().toString()));
        CampaignUser campaignUser = campaignUserRepository.findByCampaignIdAndUserId(campaign.getId(), user.getId()).orElseThrow(()->new BadCredentialsException("Invalid token for this campaign: " + authorization));

        if(campaignUser.getRole() == Role.Owner || campaignUser.getRole() == Role.Admin) {
            campaign.setName(campaignRequest.name());
            campaign.setPrivacy(campaignRequest.privacy());
            if(campaignRequest.ruleIDs() != null && !campaignRequest.ruleIDs().isEmpty()){
                List<Rule> rules = ruleRepository.findAllById(campaignRequest.ruleIDs());
                if(rules.size() != campaignRequest.ruleIDs().size()) throw new IllegalStateException("One or more rule IDs do not exist.");
                campaign.setRules(new HashSet<>(rules));
            } else campaign.setRules(new HashSet<>());
            if(campaignRequest.campaignUsers() != null){
                Set<CampaignUser> savedCampaignUsers = campaign.getCampaignUsers();
                if(campaignUser.getRole() == Role.Owner) {
                    savedCampaignUsers.removeIf(savedCampaignUser -> savedCampaignUser.getRole() != Role.Owner);
                    campaignRequest.campaignUsers().forEach(campaignUserRequest -> {
                        if(campaignUserRequest.role() == Role.Owner) return;
                        addCampaignUser(campaign, savedCampaignUsers, campaignUserRequest);
                        if(campaignUser.getUser().getActiveCampaign() == null) {
                            campaignUser.getUser().setActiveCampaign(campaign);
                            userRepository.save(campaignUser.getUser());
                        }
                    });
                } else if(campaignUser.getRole() == Role.Admin) {
                    savedCampaignUsers.removeIf(savedCampaignUser -> savedCampaignUser.getRole() != Role.Owner && savedCampaignUser.getRole() != Role.Admin);
                    campaignRequest.campaignUsers().forEach(campaignUserRequest -> {
                        if(campaignUserRequest.role() == Role.Owner || campaignUserRequest.role() == Role.Admin) return;
                        addCampaignUser(campaign, savedCampaignUsers, campaignUserRequest);
                        if(campaignUser.getUser().getActiveCampaign() == null) {
                            campaignUser.getUser().setActiveCampaign(campaign);
                            userRepository.save(campaignUser.getUser());
                        }
                    });
                }
            }
            return new CampaignDTO.CampaignResponse(campaignRepository.save(campaign));
        } else {
            throw new BadCredentialsException("Invalid token for this campaign: " + authorization);
        }
    }

    /**
     * Add a {@link User} to a {@link Campaign}.
     * @param campaignID ID of the {@link Campaign}.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return The {@link CampaignDTO.CampaignResponse Campaign} that was joined.
     */
    public CampaignDTO.CampaignResponse joinCampaign(Integer campaignID, String authorization) {
        User user = userService.validate(authorization);
        Campaign campaign = campaignRepository.findById(campaignID).orElseThrow(()-> new ResourceNotFoundException("Campaign", authorization));
        if(campaign.getCampaignUsers().stream().anyMatch(campaignUser -> Objects.equals(campaignUser.getId().getUser_id(), user.getId()))) throw new IllegalStateException("Already part of campaign.");
        Set<CampaignUser> campaignUsers = campaign.getCampaignUsers();
        CampaignUser campaignUser = new CampaignUser(new CampaignUserId(campaignID, user.getId()), campaign, user, Role.Player);
        campaignUser = campaignUserRepository.save(campaignUser);
        campaignUsers.add(campaignUser);
        campaign.setCampaignUsers(campaignUsers);
        campaignRepository.save(campaign);
        if(user.getActiveCampaign() == null) {
            user.setActiveCampaign(campaign);
            userRepository.save(user);
        }
        return new CampaignDTO.CampaignResponse(campaign);
    }

    /**
     * Link a Campaign and a User.
     * @param campaign {@link Campaign} to link.
     * @param savedCampaignUsers {@link Set<CampaignUser> Set of CampaignUsers} that is already part of the {@link Campaign}.
     * @param campaignUserRequest {@link CampaignUserDTO.CampaignUserEditRequest} with new {@link CampaignUser} data.
     */
    private void addCampaignUser(Campaign campaign, Set<CampaignUser> savedCampaignUsers, CampaignUserDTO.CampaignUserEditRequest campaignUserRequest) {
        CampaignUser newCampaignUser = new CampaignUser();
        newCampaignUser.setId(new CampaignUserId(campaign.getId(), campaignUserRequest.userID()));
        newCampaignUser.setCampaign(campaign);
        newCampaignUser.setUser(userRepository.findById(campaignUserRequest.userID()).orElseThrow(()-> new ResourceNotFoundException("User", campaignUserRequest.userID().toString())));
        newCampaignUser.setRole(campaignUserRequest.role());
        savedCampaignUsers.add(newCampaignUser);
    }

    /**
     * Get the Active {@link Campaign} for a {@link User}.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return The {@link CampaignDTO.CampaignResponse Campaign} that is set as active for the {@link User}.
     */
    public CampaignDTO.CampaignResponse getActiveCampaign(String authorization) {
        User user = userService.validate(authorization);
        if(user.getActiveCampaign() == null) throw new ResourceNotFoundException("Campaign", authorization);
        return new CampaignDTO.CampaignResponse(user.getActiveCampaign());
    }

    /**
     * Set the Active {@link Campaign} for a {@link User}.
     * @param campaignId ID of the {@link Campaign}.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return The {@link CampaignDTO.CampaignResponse Campaign} that was set as active for the {@link User}.
     */
    public CampaignDTO.CampaignResponse setActiveCampaign(Integer campaignId, String authorization) {
        User user = userService.validate(authorization);
        Campaign campaign = campaignRepository.findById(campaignId).orElseThrow(()-> new ResourceNotFoundException("Campaign", campaignId.toString()));
        if(campaign.getCampaignUsers().stream().noneMatch(campaignUser -> Objects.equals(campaignUser.getId().getUser_id(), user.getId()))) throw new IllegalStateException("User not part of the campaign.");
        user.setActiveCampaign(campaign);
        userRepository.save(user);
        return new CampaignDTO.CampaignResponse(campaign);
    }
}
