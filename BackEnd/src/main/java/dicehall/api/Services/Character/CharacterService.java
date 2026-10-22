package dicehall.api.Services.Character;

import dicehall.api.DTOs.Character.CharacterDTO;
import dicehall.api.DTOs.Character.CharacterStatDTO;
import dicehall.api.Entities.Campaign.Campaign;
import dicehall.api.Entities.Campaign.Role;
import dicehall.api.Entities.Campaign.Stat;
import dicehall.api.Entities.Character.Character;
import dicehall.api.Entities.Character.CharacterStat;
import dicehall.api.Entities.User.Token;
import dicehall.api.Entities.User.User;
import dicehall.api.Repositories.Campaign.CampaignRepository;
import dicehall.api.Repositories.Campaign.StatRepository;
import dicehall.api.Repositories.Character.CharacterRepository;
import dicehall.api.Repositories.User.UserRepository;
import dicehall.api.Security.Exceptions.ResourceNotFoundException;
import dicehall.api.Services.User.UserService;
import dicehall.api.Utility.Common;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CharacterService {

    private final UserService userService;
    private final CampaignRepository campaignRepository;
    private final StatRepository statRepository;
    private final CharacterRepository characterRepository;
    private final UserRepository userRepository;

    public CharacterService(UserService userService, CampaignRepository campaignRepository, StatRepository statRepository, CharacterRepository characterRepository, UserRepository userRepository) {
        this.userService = userService;
        this.campaignRepository = campaignRepository;
        this.statRepository = statRepository;
        this.characterRepository = characterRepository;
        this.userRepository = userRepository;
    }

    /**
     * Check if at least one name is written and set them to the {@link Character}.
     * @param firstName {@link String}
     * @param lastName {@link String}
     * @param nickName {@link String}
     * @param character the {@link Character} to set the names to.
     */
    private void setNames(String firstName, String lastName, String nickName, Character character) {
        if(firstName != null && !firstName.isBlank()) {
            character.setFirstname(firstName);
            return;
        }
        if(lastName != null && !lastName.isBlank()) {
            character.setLastname(lastName);
            return;
        }
        if(nickName != null && !nickName.isBlank()) {
            character.setNickname(nickName);
            return;
        }
        throw new IllegalStateException("Need at least one of the three name fields.");
    }

    /**
     * Parse the Selection {@link String} field into a readable {@link List<String>}.
     * @param stat The {@link Stat} with the Selection field to parse.
     * @return a {@link List<String>}.
     */
    static public List<String> parseSelection(Stat stat){
        if (stat.getSelection() == null || stat.getSelection().isBlank()) return new ArrayList<>();
        return Arrays.stream(stat.getSelection().split(Pattern.quote("|||"))).map(String::trim).filter(selection -> !selection.isEmpty()).collect(Collectors.toList());
    }

    /**
     * Validate if the value entered by {@link User} is one of the options in the Selection field.
     * @param value The {@link String} value entered by {@link User}.
     * @param stat The {@link Stat} with the Selection field.
     * @return The trimmed value if correct.
     */
    private String validateSelectionValue(String value, Stat stat){
        if(parseSelection(stat).contains(value.trim())) {
            return value.trim();
        } else {
            throw new IllegalStateException("Value '" + value.trim() + "' isn't part of the available options.");
        }
    }

    /**
     * Check the {@link Stat} for any errors and set it to the {@link Character}
     * @param value The {@link String} value entered by {@link User}.
     * @param statID The {@link Integer id} of the {@link Stat} that is added to the {@link Character}.
     * @param campaignID The {@link Integer id} of the {@link Campaign} of the {@link Character}.
     * @param character The {@link Character} the {@link Stat} is gonna be added to.
     * @return a {@link CharacterStat} that can be added to a {@link Character}.
     */
    private CharacterStat createCharacterStat(String value, Integer statID, Integer campaignID, Character character) {
        CharacterStat characterStat = new CharacterStat();
        characterStat.setCharacter(character);
        Stat stat = statRepository.findByStatAndCampaignId(statID, campaignID).orElseThrow(() -> new ResourceNotFoundException("Stat", statID.toString()));
        characterStat.setStat(stat);
        if(value == null || value.isBlank()) {
            //If Blank or null
            //Check if required
            if(stat.getRequired()) {
                //Set Default value if available
                if(stat.getDefaultValue() != null) {
                    characterStat.setValue(stat.getDefaultValue());
                } else {
                    throw new IllegalStateException("Value required for stat \"" + stat.getName() + "\".");
                }
            } else if(stat.getMax() != null || stat.getMin() != null) {
                //If Integer set to 0
                characterStat.setValue(String.valueOf(0));
            } else {
                //Otherwise try to set to Default value
                characterStat.setValue(stat.getDefaultValue());
            }
        } else {
            //If Integer check for max and min value
            if(stat.getMax() != null || stat.getMin() != null) {
                if(Common.isInteger(value)) {
                    Integer intValue = Integer.valueOf(value);
                    if (stat.getMin() != null && intValue < stat.getMin())
                        throw new IllegalStateException("Stat \"" + stat.getName() + "\" is inferior to minimum value \"" + stat.getMin() + "\".");
                    if (stat.getMax() != null && intValue > stat.getMax())
                        throw new IllegalStateException("Stat \"" + stat.getName() + "\" is superior to maximum value \"" + stat.getMax() + "\".");
                    characterStat.setValue(value);
                } else {
                    throw new IllegalStateException("Stat \"" + stat.getName() + "\" should be a number.");
                }
            } else {
                //If Selection field, validate the value
                if(stat.getSelection() != null && !stat.getSelection().isBlank()) {
                    characterStat.setValue(validateSelectionValue(value, stat));
                }
                characterStat.setValue(value);
            }
        }
        return characterStat;
    }

    /**
     * Check if {@link User} sent all required {@link Stat Stats}, Verify all the {@link Stat Stats} and add them all to his {@link Character}.
     * @param characterStatsRequest {@link CharacterStatDTO.CharacterStatRequest CharacterStatRequest} containing the value inputted and the id of the concerned {@link Stat}.
     * @param campaign the {@link Campaign} the {@link Character} is linked to.
     * @param character the {@link Character} to add the {@link Stat Stats} to.
     */
    private void addCharacterStats(List<CharacterStatDTO.CharacterStatRequest> characterStatsRequest, Campaign campaign, Character character) {
        List<Stat> requiredStats = campaign.getRules().stream().flatMap(rule -> rule.getStats().stream()).filter(stat -> Boolean.TRUE.equals(stat.getRequired())).distinct().collect(Collectors.toCollection(ArrayList::new));
        Set<CharacterStat> characterStats = new HashSet<>();
        characterStatsRequest.forEach(characterStatRequest -> {
            CharacterStat characterStat = createCharacterStat(characterStatRequest.value(), characterStatRequest.statID(), campaign.getId(), character);
            characterStats.add(characterStat);
            requiredStats.remove(characterStat.getStat());
        });
        if(!requiredStats.isEmpty()) {
            String requiredFields = "[" + requiredStats.stream().map(Stat::getName).collect(Collectors.joining(", ")) + "]";
            throw new IllegalStateException("Required Stats were not supplied: " + requiredFields);
        }
        character.getCharacterStats().removeIf(characterStat -> characterStats.stream().noneMatch(characterStatToRemove -> Objects.equals(characterStatToRemove.getStat().getId(), characterStat.getStat().getId()) && Objects.equals(characterStatToRemove.getValue(), characterStat.getValue())));
        characterRepository.save(character);
        characterStats.forEach(characterStatToAdd -> {
            if (character.getCharacterStats().stream().noneMatch(characterStat -> Objects.equals(characterStat.getStat().getId(), characterStatToAdd.getStat().getId()))) character.getCharacterStats().add(characterStatToAdd);
        });
    }

    /**
     * If a {@link User} try to set a {@link Character} to another {@link User} check if they have the right to do so.
     * @param userID The id of the {@link User} that the {@link Character} should be linked to.
     * @param user {@link User} that is doing the operation.
     * @param campaign The {@link Campaign} the {@link Character} is linked to.
     */
    private void canSetUser(Integer userID, User user, Campaign campaign) {
        if(userID != null && !Objects.equals(userID, user.getId()) && campaign.getCampaignUsers().stream().noneMatch(campaignUser -> Objects.equals(campaignUser.getId().getUser_id(), user.getId()) && (campaignUser.getRole() == Role.Owner || campaignUser.getRole() == Role.Admin))) throw new IllegalStateException("You can't set the character to another User if you are not the Owner or an Admin of the Campaign.");
    }

    /**
     * Create a {@link Character} and make sure every value is correct.
     * @param request {@link CharacterDTO.CharacterCreateRequest CharacterCreateRequest} with {@link Character} data.
     * @param authorization  The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return The {@link CharacterDTO.CharacterResponse Character} that was created.
     */
    public CharacterDTO.CharacterResponse create(CharacterDTO.CharacterCreateRequest request, String authorization) {
        User user = userService.validate(authorization);
        Campaign campaign = campaignRepository.findById(request.campaignID()).orElseThrow(() -> new ResourceNotFoundException("Campaign", request.campaignID().toString()));
        Character character = new Character();
        if(campaign.getCampaignUsers().stream().noneMatch(campaignUser -> Objects.equals(campaignUser.getId().getCampaign_id(), request.campaignID()) && Objects.equals(campaignUser.getUser().getId(), user.getId()))) throw new AccessDeniedException("You are not part of the Campaign.");
        if(campaign.getCampaignUsers().stream().noneMatch(campaignUser -> Objects.equals(campaignUser.getId().getCampaign_id(), request.campaignID()) && Objects.equals(campaignUser.getUser().getId(), request.userID()))) throw new IllegalStateException("The selected User is not part of the Campaign.");
        addCharacterStats(request.characterStats(), campaign, character);
        canSetUser(request.userID(), user, campaign);
        character.setUser(request.userID() != null ? userRepository.findById(request.userID()).orElseThrow(()->new ResourceNotFoundException("User", request.userID().toString())) : user);
        setNames(request.firstname(), request.lastname(), request.nickname(), character);
        character.setValidate(campaign.getCampaignUsers().stream().anyMatch(campaignUser -> Objects.equals(campaignUser.getId().getCampaign_id(), request.campaignID()) && Objects.equals(campaignUser.getUser().getId(), user.getId()) && (campaignUser.getRole() == Role.Admin || campaignUser.getRole() == Role.Owner)));
        character.setCampaign(campaign);

        return new CharacterDTO.CharacterResponse(characterRepository.save(character));
    }

    /**
     * Edit a {@link Character} and make sure every value is correct.
     * @param request {@link CharacterDTO.CharacterEditRequest CharacterEditRequest} with {@link Character} data.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return The {@link CharacterDTO.CharacterResponse Character} that was edited.
     */
    public CharacterDTO.CharacterResponse edit(CharacterDTO.CharacterEditRequest request, String authorization) {
        User user = userService.validate(authorization);
        Character character = characterRepository.findById(request.id()).orElseThrow(() -> new ResourceNotFoundException("Character", request.id().toString()));
        Campaign campaign = character.getCampaign();
        if(campaign.getCampaignUsers().stream().noneMatch(campaignUser -> Objects.equals(campaignUser.getId().getCampaign_id(), campaign.getId()) && Objects.equals(campaignUser.getUser().getId(), user.getId()))) throw new IllegalStateException("You are not part of the Campaign.");
        if(campaign.getCampaignUsers().stream().noneMatch(campaignUser -> Objects.equals(campaignUser.getId().getCampaign_id(), campaign.getId()) && Objects.equals(campaignUser.getUser().getId(), request.userID()))) throw new IllegalStateException("The selected User is not part of the Campaign.");
        addCharacterStats(request.characterStats(), campaign, character);
        canSetUser(request.userID(), user, campaign);
        character.setUser(request.userID() != null ? userRepository.findById(request.userID()).orElseThrow(()->new ResourceNotFoundException("User", request.userID().toString())) : user);
        setNames(request.firstname(), request.lastName(), request.nickname(), character);
        character.setValidate(campaign.getCampaignUsers().stream().anyMatch(campaignUser -> Objects.equals(campaignUser.getId().getCampaign_id(), campaign.getId()) && Objects.equals(campaignUser.getUser().getId(), user.getId()) && (campaignUser.getRole() == Role.Admin || campaignUser.getRole() == Role.Owner)));
        character.setCampaign(campaign);

        return new CharacterDTO.CharacterResponse(characterRepository.save(character));
    }

    /**
     * Validate a {@link Character}.
     * Only an {@link Role#Owner Owner} or {@link Role#Admin Admins} can validate a {@link Character}
     * @param request {@link CharacterDTO.CharacterEditRequest CharacterEditRequest} with Character Validation data.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return The {@link CharacterDTO.CharacterResponse Character} that was validated.
     */
    public CharacterDTO.CharacterResponse validate(CharacterDTO.CharacterValidateRequest request, String authorization) {
        User user = userService.validate(authorization);
        Character character = characterRepository.findById(request.characterID()).orElseThrow(() -> new ResourceNotFoundException("Character", request.characterID().toString()));
        Campaign campaign = character.getCampaign();
        if(campaign.getCampaignUsers().stream().noneMatch(campaignUser -> Objects.equals(campaignUser.getId().getCampaign_id(), campaign.getId()) && Objects.equals(campaignUser.getUser().getId(), user.getId()))) throw new IllegalStateException("You are not part of the Campaign.");
        if(campaign.getCampaignUsers().stream().noneMatch(campaignUser -> Objects.equals(campaignUser.getId().getCampaign_id(), campaign.getId()) && Objects.equals(campaignUser.getUser().getId(), user.getId()) && (campaignUser.getRole() == Role.Admin || campaignUser.getRole() == Role.Owner))) throw new AccessDeniedException("You can't validate a character if you are not the Owner or an Admin of the Campaign.");
        character.setValidate(true);
        return new CharacterDTO.CharacterResponse(characterRepository.save(character));
    }
}
