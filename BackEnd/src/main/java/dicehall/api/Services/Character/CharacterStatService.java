package dicehall.api.Services.Character;

import dicehall.api.Entities.Campaign.Campaign;
import dicehall.api.Entities.Campaign.Stat;
import dicehall.api.Entities.Character.Character;
import dicehall.api.Entities.Character.CharacterStat;
import dicehall.api.Entities.User.User;
import dicehall.api.Repositories.Campaign.StatRepository;
import dicehall.api.Repositories.Character.CharacterRepository;
import dicehall.api.Security.Exceptions.ResourceNotFoundException;
import dicehall.api.Utility.Common;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CharacterStatService {
    private final StatRepository statRepository;
    private final CharacterRepository characterRepository;

    public CharacterStatService(StatRepository statRepository, CharacterRepository characterRepository) {
        this.statRepository = statRepository;
        this.characterRepository = characterRepository;
    }

    /**
     * Parse the Selection {@link String} field into a readable {@link List <String>}.
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
    public String validateSelectionValue(String value, Stat stat){
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
    public CharacterStat createCharacterStat(String value, Integer statID, Integer campaignID, Character character) {

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
                    int intValue = Integer.parseInt(value);
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
                } else {
                    characterStat.setValue(value);
                }
            }
        }
        return characterStat;
    }

    /**
     * Check the {@link Stat} for any errors and replace it in the {@link Character}
     * @param receivedCharacterStat The edited {@link CharacterStat}.
     * @param character The {@link Character} the {@link Stat} is gonna be replaced.
     */
    public void editCharacterStatsFromRule(CharacterStat receivedCharacterStat, Character character) {
        Set<CharacterStat> characterStats = new HashSet<>();
        CharacterStat characterStat = createCharacterStat(receivedCharacterStat.getValue(), receivedCharacterStat.getId().getStat_id(), character.getCampaign().getId(), character);
        characterStats.add(characterStat);
        if(characterStat.getStat().getRequired() && (characterStat.getValue() == null || characterStat.getValue().isEmpty())) throw new IllegalStateException("Required Stat was not supplied: " + characterStat.getStat().getName());
        character.getCharacterStats().removeIf(stat -> characterStats.stream().anyMatch(characterStatToRemove -> Objects.equals(characterStatToRemove.getStat().getId(), stat.getStat().getId()) && !Objects.equals(characterStatToRemove.getValue(), stat.getValue())));
        characterRepository.save(character);
        characterStats.forEach(characterStatToAdd -> {
            if (character.getCharacterStats().stream().noneMatch(stat -> Objects.equals(stat.getStat().getId(), characterStatToAdd.getStat().getId()))) character.getCharacterStats().add(characterStatToAdd);
        });
        characterRepository.save(character);
    }
}
