package dicehall.api.Services.Character;

import dicehall.api.DTOs.Character.CharacterDTO;
import dicehall.api.DTOs.Character.CharacterStatDTO;
import dicehall.api.Entities.Campaign.Stat;
import dicehall.api.Entities.Campaign.StatCheckpoint;
import dicehall.api.Entities.Character.Character;
import dicehall.api.Entities.Character.CharacterStat;
import dicehall.api.Entities.Character.CharacterStatId;
import dicehall.api.Entities.Rule.Rule;
import dicehall.api.Entities.User.Token;
import dicehall.api.Entities.User.User;
import dicehall.api.Repositories.Character.CharacterRepository;
import dicehall.api.Repositories.Character.CharacterStatRepository;
import dicehall.api.Security.Exceptions.ResourceNotFoundException;
import dicehall.api.Services.Rule.RuleService;
import dicehall.api.Services.User.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RollService {
    private final UserService userService;
    private final CharacterRepository characterRepository;
    private final RuleService ruleService;
    private final CharacterStatRepository characterStatRepository;

    public RollService(UserService userService, CharacterRepository characterRepository, RuleService ruleService, CharacterStatRepository characterStatRepository) {
        this.userService = userService;
        this.characterRepository = characterRepository;
        this.ruleService = ruleService;
        this.characterStatRepository = characterStatRepository;
    }

    /**
     * Get a random value for a {@link Stat}.
     * Stay between 1 and the dice value of the {@link Stat}.
     * @param request The {@link Stat}, {@link Character} and list of {@link Stat Stats} that shouldn't be handled by Rules.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return {@link CharacterStatDTO.CharacterStatRollRequest} containing the {@link CharacterDTO.CharacterResponse}, the rolled value and corresponding name.
     */
    public CharacterDTO.CharacterRollResponse rollStat(CharacterStatDTO.CharacterStatRollRequest request, String authorization) {
        User user = userService.validate(authorization);
        Character character = characterRepository.findById(request.characterID()).orElseThrow(() -> new ResourceNotFoundException("Character", request.characterID().toString()));
        if(!Objects.equals(character.getUser().getId(), user.getId())) throw new AccessDeniedException("You need to be the Character's Owner to roll a dice on his Stats.");
        if (character.getCharacterStats().stream().noneMatch(characterStatIn -> Objects.equals(characterStatIn.getId().getStat_id(), request.statID()))) throw new ResourceNotFoundException("CharacterStat", request.statID().toString());
        List<Rule> checkedRules = new ArrayList<>();
        List<CharacterStatDTO.CharacterStatResponse> characterStats = ruleService.handleRules(character, request.noAutoStatIDs(), null, checkedRules);
        CharacterStatDTO.CharacterStatResponse characterStat = characterStats.stream().filter(characterStatIn -> Objects.equals(characterStatIn.campaignStat().id(), request.statID())).findFirst().orElseThrow(() -> new ResourceNotFoundException("CharacterStat", request.statID().toString()));
        int roll = ThreadLocalRandom.current().nextInt(1, characterStat.campaignStat().dice() + 1);
        checkedRules.removeIf(rule -> !rule.getOperation().trim().endsWith("_DEF_"));
        characterStats = ruleService.handleRules(character, request.noAutoStatIDs(), request.statID(), checkedRules);


        return new CharacterDTO.CharacterRollResponse(roll, getRolledValueSuccessName(roll, characterStatRepository.findById(new CharacterStatId(character.getId(), request.statID())).orElseThrow(()->new RuntimeException("Error with code: R1")), characterStat), new CharacterDTO.CharacterResponse(character, characterStats));
    }

    /**
     * Get the name the rolled value have (success / miss ect..).
     * @param roll The value that was rolled.
     * @param characterStat The stat that is being rolled.
     * @param realCharacterStat The characterStat that's been passed through the rules.
     * @return The text that tells what the rolled value equals to.
     */
    public String getRolledValueSuccessName(int roll, CharacterStat characterStat, CharacterStatDTO.CharacterStatResponse realCharacterStat) {
        List<StatCheckpoint> underCheckpoints = characterStat.getStat().getStatCheckpoints().stream().filter(checkpoint -> !checkpoint.getSuperior()).sorted(Comparator.comparing(StatCheckpoint::getValue, Comparator.nullsLast(Comparator.naturalOrder()))).toList();
        List<StatCheckpoint> overCheckpoints = characterStat.getStat().getStatCheckpoints().stream().filter(StatCheckpoint::getSuperior).sorted(Comparator.comparing(StatCheckpoint::getValue, Comparator.nullsLast(Comparator.reverseOrder()))).toList();
        for (StatCheckpoint underCheckpoint : underCheckpoints) {
            if(!underCheckpoint.getToSuccess()){
                if(roll <= underCheckpoint.getValue()) return underCheckpoint.getName();
            } else {
                if(roll <= (realCharacterStat.editedValue() == null ? Integer.parseInt(realCharacterStat.originalValue()) : Integer.parseInt(realCharacterStat.editedValue()))) return underCheckpoint.getName();
            }
        }
        for (StatCheckpoint overCheckpoint : overCheckpoints) {
            if(!overCheckpoint.getToSuccess()){
                if(roll >= overCheckpoint.getValue()) return overCheckpoint.getName();
            } else {
                if(roll > (realCharacterStat.editedValue() == null ? Integer.parseInt(realCharacterStat.originalValue()) : Integer.parseInt(realCharacterStat.editedValue()))) return overCheckpoint.getName();
            }
        }
        throw new IllegalStateException("No success value found for this roll.");
    }
}
