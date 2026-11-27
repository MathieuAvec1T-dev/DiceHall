package dicehall.api.Services.Rule;

import dicehall.api.DTOs.Campaign.StatDTO;
import dicehall.api.DTOs.Character.CharacterStatDTO;
import dicehall.api.Entities.Campaign.Campaign;
import dicehall.api.Entities.Campaign.Stat;
import dicehall.api.Entities.Campaign.StatCheckpoint;
import dicehall.api.Entities.Character.Character;
import dicehall.api.Entities.Character.CharacterStat;
import dicehall.api.Entities.Rule.Rule;
import dicehall.api.Repositories.Campaign.StatRepository;
import dicehall.api.Security.Exceptions.ResourceNotFoundException;
import dicehall.api.Services.Character.CharacterStatService;
import dicehall.api.Utility.Common;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class RuleService {
    private final StatRepository statRepository;

    //CampaignID > Rules
    private final ConcurrentHashMap<Integer, ArrayList<Rule>> campaignToRuleMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, ArrayList<Rule>> campaignToRuleBEFOREOnlyMap = new ConcurrentHashMap<>();
    private final CharacterStatService characterStatService;

    public RuleService(StatRepository statRepository, CharacterStatService characterStatService) {
        this.statRepository = statRepository;
        this.characterStatService = characterStatService;
    }

    public record ParsedRule(boolean condition, List<String> result, boolean definitive) {}

    /**
     * Describe whe the Rule should be Triggered:<br>
     * - BEFORE = Before the roll and will also trigger on roll so that the roll uses the correct value.<br>
     * - ON = Only when a Roll is done on a stat in the comparison part of a Rule.
     */
    public enum RuleTrigger {
        BEFORE,
        ON,
    }

    /**
     * Get the value of a {@link Stat} the {@link Character} might have in a {@link CharacterStatDTO.CharacterStatSimplified}.
     * <br> Use {@link #parseValue} if you want the String value only.
     * @param value the stat's ID written in a {@link Rule} (example: {@code "#statID"}).
     * @param characterStats the list of {@link CharacterStatDTO.CharacterStatSimplified CharacterStats} that hold all the edited and original {@link CharacterStat CharacterStats}.
     * @param character the {@link Character} the {@link CharacterStat CharacterStats} are linked to.
     * @return the {@link CharacterStatDTO.CharacterStatSimplified} with the corresponding value if the {@link Character} possess it, {@code null} otherwise.
     */
    private @Nullable CharacterStatDTO.CharacterStatSimplified getCharacterStat(String value, List<CharacterStatDTO.CharacterStatSimplified> characterStats, Character character) {
        if(campaignHasStat(value.trim(), character)) {
            characterStats = characterStats.stream().filter(characterStat -> characterStat.statID() == Integer.parseInt(value.replace("#", "").trim())).toList();
            if(characterStats.isEmpty()) return null;
            return characterStats.getFirst();
        }
        return null;
    }

    /**
     * Get the String value of a {@link Stat} the {@link Character} might have.
     * <br> Use {@link #getCharacterStat} if you want the full {@link CharacterStatDTO.CharacterStatSimplified} object.
     * @param value the stat's ID written in a {@link Rule} (example: {@code "#statID"}).
     * @param characterStats the list of {@link CharacterStatDTO.CharacterStatSimplified CharacterStats} that hold all the edited and original {@link CharacterStat CharacterStats}.
     * @param character the {@link Character} the {@link CharacterStat CharacterStats} are linked to.
     * @return the value of the {@link Stat} if the {@link Character} possess it, {@code null} otherwise.
     */
    private @Nullable String parseValue(String value, List<CharacterStatDTO.CharacterStatSimplified> characterStats, Character character) {
        CharacterStatDTO.CharacterStatSimplified characterStat = getCharacterStat(value, characterStats, character);
        return characterStat == null ? null : characterStat.value().trim();
    }

    /**
     * Check if a {@link Campaign} possess a {@link Stat} by using the stat's ID.
     * @param value the stat's ID written in a {@link Rule} (example: {@code "#statID"}).
     * @param character the {@link Character} the {@link CharacterStat CharacterStats} are linked to.
     * @return {@code True} if the {@link Campaign} possess the {@code Stat}, {@code False} otherwise.
     */
    private boolean campaignHasStat(String value, Character character){
        if(value.contains("#") && Common.isInteger(value.replace("#", "").trim())) {
            return statRepository.findByStatAndCampaignId(Integer.parseInt(value.replace("#", "").trim()), character.getCampaign().getId()).isPresent();
        }
        return false;
    }

    /**
     * Separate a condition between the first part with stats, comparators and values(and other possible values).
     * @param splitCondition The list of string from the comparison separated by the comparator.
     * @param characterStats  the list of {@link CharacterStatDTO.CharacterStatSimplified CharacterStats} that hold all the edited and original {@link CharacterStat CharacterStats}.
     * @param character the {@link Character} the {@link CharacterStat CharacterStats} are linked to.
     * @return a List of two List of Strings. The first List hold the first part of the comparison with one or multiple stats depending on if All or NONE was used. The second List is the value or stat value(or other possible values).
     */
    private List<List<String>> separateCondition(List<String> splitCondition, List<CharacterStatDTO.CharacterStatSimplified> characterStats, Character character){
        List<String> partOne = new ArrayList<>(List.of(splitCondition.getFirst().trim()));
        List<String> partTwo = splitCondition.size() < 2 ? new ArrayList<>() : new ArrayList<>(List.of(splitCondition.get(1).trim()));
        if(splitCondition.getFirst().trim().equals("ALL") || splitCondition.getFirst().trim().equals("NONE")){
            partOne.removeFirst();
            partOne.addAll(characterStats.stream().map(characterStat -> characterStat.value().trim()).toList());
        } else if(campaignHasStat(splitCondition.getFirst().trim(), character)) {
            String value = parseValue(splitCondition.getFirst(), characterStats, character);
            partOne.set(0, value);
        } else {
            throw new IllegalStateException("Wrong argument in condition: Need a '#StatID' that exists in campaign or 'ALL' or 'NONE' as the first part of the condition.");
        }
        if(splitCondition.size() >= 2) {
            if(splitCondition.get(1).contains("PERCENT(") && splitCondition.get(1).contains(",") && splitCondition.get(1).contains(")")) {
                List<String> percentageValues = new ArrayList<>(Arrays.stream(splitCondition.get(1).replace("PERCENT(", "").replace(")", "").split(",")).toList());
                if(!percentageValues.getFirst().contains("#")) throw new IllegalStateException("Wrong argument in condition: If you use a PERCENT value of a stat the first part of PERCENT(#statID,percentage) need to be a statID that exists in campaign.");
                percentageValues.set(0 , percentageValues.getFirst().replace("#", ""));
                if(!Common.isInteger(percentageValues.getFirst().trim()) || !Common.isInteger(percentageValues.getLast().trim())) throw new IllegalStateException("Wrong argument in condition: If you use a PERCENT value of a stat both the stat and the percentage needs to be number.");
                if(!campaignHasStat("#"+percentageValues.getFirst().trim(), character)) throw new IllegalStateException("Wrong argument in condition: If you use a PERCENT value of a stat the first part of PERCENT(#statID,percentage) need to be a statID that exists in campaign.");
                String statValue = parseValue("#"+percentageValues.getFirst().trim(), characterStats, character);
                if(statValue == null || statValue.isBlank() || !Common.isInteger(statValue.trim()))throw new IllegalStateException("Wrong argument in condition: If you use a PERCENT value of a stat both the stat and the percentage needs to be number.");
                partTwo.set(0, String.valueOf(Integer.parseInt(statValue.trim()) * Integer.parseInt(percentageValues.getLast().trim()) / 100));
            } else {
                String value = parseValue(splitCondition.get(1), characterStats, character);
                partTwo.set(0, value == null ? splitCondition.get(1).trim() : value);
            }
        }
        return List.of(partOne, partTwo);
    }

    /**
     * Check the whole Comparison part of a {@link Rule}. Take in account the AND and OR.
     * @param condition the String containing only the condition part of a {@link Rule#getOperation()}.
     * @param characterStats the list of {@link CharacterStatDTO.CharacterStatSimplified CharacterStats} that hold all the edited and original {@link CharacterStat CharacterStats}.
     * @param character the {@link Character} the {@link CharacterStat CharacterStats} are linked to.
     * @return {@code True} or {@code False} depending on the result of the Comparison.
     */
    private boolean checkCondition(String condition, List<CharacterStatDTO.CharacterStatSimplified> characterStats, Character character, String rolledValueType, Integer statIdRolled, Integer rolledValue){
        //Separate each operation for each AND in an array that way if one is fully true it stops searching, if one is false it goes to the next array.
        List<List<String>> operationsOR = Arrays.stream(condition.split("(?=\\bOR\\b)|(?<=\\bOR\\b)"))
                .filter(operation -> !operation.equals("OR"))
                .map(operation -> Arrays.stream(operation.split("\\s+AND\\s+")).map(String::strip).collect(Collectors.toCollection(ArrayList::new)))
                .collect(Collectors.toCollection(ArrayList::new));
        boolean conditionValue = false;
        for (List<String> operations : operationsOR) {
            boolean check = true;
            for (String operation : operations) {
                if(!checkComparison(operation, characterStats, character, rolledValueType, statIdRolled, rolledValue)) {
                    check = false;
                    break;
                }
            }
            if(check) {
                conditionValue = true;
                break;
            }
        }
        return conditionValue;
    }

    /**
     * Check the Rolled condition.
     * @param characterStats the list of {@link CharacterStatDTO.CharacterStatSimplified CharacterStats} that hold all the edited and original {@link CharacterStat CharacterStats}.
     * @param idOfStat ID of the {@link Stat} that is in the first part of the condition.
     * @param statIDRolled ID of the {@link Stat} that was rolled.
     * @param rolledValueType Name of the {@link StatCheckpoint} corresponding to the rolled value.
     * @param rolledValue The value Rolled.
     * @param conditionWithParenthesis The condition on the right side of "ROLLED".
     * @return {@code True} if the condition is True, {@code False} otherwise.
     */
    private boolean checkRolled(List<CharacterStatDTO.CharacterStatSimplified> characterStats, String idOfStat, Integer statIDRolled, String rolledValueType, Integer rolledValue, String conditionWithParenthesis) {
        idOfStat = idOfStat.trim();
        if(!Common.isInteger(idOfStat)) throw new IllegalStateException("Wrong argument in condition: If you use 'ROLLED' the part in front must be a stat ID.");
        if(statIDRolled == null) throw new RuntimeException("Error with code: Ro1");
        if(!Objects.equals(Integer.parseInt(idOfStat), statIDRolled)) return false;
        if(conditionWithParenthesis == null || conditionWithParenthesis.isBlank()) return true;
        if(!conditionWithParenthesis.contains("(") || !conditionWithParenthesis.contains(")")) throw new IllegalStateException("Wrong argument in condition: If you use 'ROLLED' the part after must be either a StatCheckpointID OR a comparator a comma and a StatCheckpointID.");
        if(rolledValueType == null) throw new RuntimeException("Error with code: Ro2");
        if(rolledValue == null) throw new RuntimeException("Error with code: Ro3");
        conditionWithParenthesis = conditionWithParenthesis.replace("(","").replace(")","");
        if(!conditionWithParenthesis.contains(",")) {
            String finalConditionWithParenthesis = conditionWithParenthesis;
            Optional<StatCheckpoint> checkpointOpt = statRepository.findById(Integer.parseInt(idOfStat)).orElseThrow(()-> new IllegalStateException("Wrong argument in condition: Need a '#StatID' that exists in campaign or 'ALL' or 'NONE' as the first part of the condition.")).getStatCheckpoints().stream().filter(statCheckpoint-> Objects.equals(statCheckpoint.getId(), Integer.parseInt(finalConditionWithParenthesis))).findFirst();
            if(checkpointOpt.isEmpty()) return false;
            StatCheckpoint checkpoint = checkpointOpt.get();
            return Objects.equals(checkpoint.getName(), rolledValueType);
        } else {
            List<String> conditionList = Arrays.stream(conditionWithParenthesis.split(",")).toList();
            if(conditionList.size() != 2) throw new IllegalStateException("Wrong argument in condition: If you use 'ROLLED' the part after must be either a StatCheckpointID OR a comparator a comma and a StatCheckpointID.");
            Stat stat = statRepository.findById(Integer.parseInt(idOfStat)).orElseThrow(()-> new IllegalStateException("Wrong argument in condition: Need a '#StatID' that exists in campaign or 'ALL' or 'NONE' as the first part of the condition."));
            Optional<StatCheckpoint> checkpointOpt = stat.getStatCheckpoints().stream().filter(statCheckpoint-> Objects.equals(statCheckpoint.getId(), Integer.parseInt(conditionList.getLast()))).findFirst();
            if(checkpointOpt.isEmpty()) return false;
            StatCheckpoint checkpoint = checkpointOpt.get();
            int checkpointValue = checkpoint.getValue() == null ? Integer.parseInt(characterStats.stream().filter(characterStat -> characterStat.statID().equals(statIDRolled)).findFirst().orElseThrow(()-> new RuntimeException("Error with code: Ro4")).value().trim()) : checkpoint.getValue();
            switch (conditionList.getFirst()) {
                case String s when s.contains("<=") -> {return checkpoint.getName().equals(rolledValueType) || rolledValue <= checkpointValue;}
                case String s when s.contains("<") -> {return rolledValue < checkpointValue;}
                case String s when s.contains(">=") -> {return checkpoint.getName().equals(rolledValueType) || rolledValue >= checkpointValue;}
                case String s when s.contains(">") -> {return rolledValue > checkpointValue;}
                case String s when s.contains("!=") -> {return !checkpoint.getName().equals(rolledValueType);}
                case String s when s.contains("=") -> {return checkpoint.getName().equals(rolledValueType);}
                default -> throw new IllegalStateException("Wrong argument in condition: If you use 'ROLLED' the comparators you can use in the parenthesis are '<,<=,>,>=,='.");
            }
        }
    }

    /**
     * Checks a Comparison of a {@link Rule}.
     * @param operation the {@link Rule#getOperation()}.
     * @param characterStats the list of {@link CharacterStatDTO.CharacterStatSimplified CharacterStats} that hold all the edited and original {@link CharacterStat CharacterStats}.
     * @param character the {@link Character} the {@link CharacterStat CharacterStats} are linked to.
     * @param rolledValue The value Rolled.
     * @param statIDRolled ID of the {@link Stat} that was rolled.
     * @param rolledValueType Name of the {@link StatCheckpoint} corresponding to the rolled value.
     * @return {@code True} or {@code False} depending on the result of the Comparison.
     */
    private boolean checkComparison(String operation, List<CharacterStatDTO.CharacterStatSimplified> characterStats, Character character, @Nullable String rolledValueType, @Nullable Integer statIDRolled, @Nullable Integer rolledValue) {
        boolean condition = false;
        ArrayList<String> splitCondition;
        switch (operation){
            case String s when s.contains("ROLLED") -> splitCondition = new ArrayList<>(Arrays.asList(s.split("ROLLED")));
            case String s when s.contains("<=") -> splitCondition = new ArrayList<>(Arrays.asList(s.split("<=")));
            case String s when s.contains("<") -> splitCondition = new ArrayList<>(Arrays.asList(s.split("<")));
            case String s when s.contains(">=") -> splitCondition = new ArrayList<>(Arrays.asList(s.split(">=")));
            case String s when s.contains(">") -> splitCondition = new ArrayList<>(Arrays.asList(s.split(">")));
            case String s when s.contains("!=") -> splitCondition = new ArrayList<>(Arrays.asList(s.split("!=")));
            case String s when s.contains("=") -> splitCondition = new ArrayList<>(Arrays.asList(s.split("=")));
            case String s when s.contains("!EXIST") || s.contains("NOTEXIST") -> splitCondition = new ArrayList<>(Arrays.asList(s.split("!EXIST")));
            case String s when s.contains("EXIST") -> splitCondition = new ArrayList<>(Arrays.asList(s.split("EXIST")));
            default -> throw new IllegalStateException("Wrong argument in condition: Operator can only be '= != < <= > >= EXIST !EXIST ROLLED'.");
        }
        if(!splitCondition.getFirst().contains("#") && !splitCondition.getFirst().contains("ALL") && !splitCondition.getFirst().contains("NONE")) throw new IllegalStateException("Wrong argument in condition: Need a '#StatID' or 'ALL' or 'NONE' as the first part of the condition.");
        List<List<String>> separatedCondition = separateCondition(splitCondition, characterStats, character);
        if(splitCondition.getFirst().trim().equals("ALL")) condition = true;
        if(splitCondition.getFirst().trim().equals("NONE")) condition = true;
        for (String value : separatedCondition.getFirst()) {
            Boolean baseCondition = null;
            switch (operation){
                case String s when s.contains("ROLLED") -> baseCondition = (statIDRolled == null || rolledValueType == null || rolledValue == null) || !checkRolled(characterStats, splitCondition.getFirst().replace("#",""), statIDRolled, rolledValueType, rolledValue, separatedCondition.size() > 1 ? separatedCondition.get(1).getFirst():null);
                case String s when s.contains("<=") -> {if(Common.isInteger(value) && Common.isInteger(separatedCondition.get(1).getFirst())) baseCondition = Integer.parseInt(value) > Integer.parseInt(separatedCondition.get(1).getFirst());}
                case String s when s.contains("<") -> {if(Common.isInteger(value) && Common.isInteger(separatedCondition.get(1).getFirst())) baseCondition = Integer.parseInt(value) >= Integer.parseInt(separatedCondition.get(1).getFirst());}
                case String s when s.contains(">=") -> {if(Common.isInteger(value) && Common.isInteger(separatedCondition.get(1).getFirst())) baseCondition = Integer.parseInt(value) < Integer.parseInt(separatedCondition.get(1).getFirst());}
                case String s when s.contains(">") -> {if(Common.isInteger(value) && Common.isInteger(separatedCondition.get(1).getFirst())) baseCondition = Integer.parseInt(value) <= Integer.parseInt(separatedCondition.get(1).getFirst());}
                case String s when s.contains("!=") -> baseCondition = Objects.equals(value, separatedCondition.get(1).getFirst());
                case String s when s.contains("=") -> baseCondition = !Objects.equals(value, separatedCondition.get(1).getFirst());
                case String s when s.contains("!EXIST") || s.contains("NOTEXIST") -> {if(Common.isInteger(splitCondition.getFirst().trim().replace("#",""))) {baseCondition = character.getCharacterStats().stream().anyMatch(stat -> stat.getId().getStat_id() == Integer.parseInt(splitCondition.getFirst().trim().replace("#","")));}}
                case String s when s.contains("EXIST") -> {if(Common.isInteger(splitCondition.getFirst().trim().replace("#",""))) {baseCondition = character.getCharacterStats().stream().noneMatch(stat -> stat.getId().getStat_id() == Integer.parseInt(splitCondition.getFirst().trim().replace("#","")));}}
                default -> throw new IllegalStateException("Wrong argument in condition: Operator can only be '= != < <= > >= EXIST !EXIST ROLLED'.");
            }
            if(baseCondition != null){
                if(baseCondition){
                    if(splitCondition.getFirst().trim().equals("ALL")) {
                        condition = false;
                    } else if(!splitCondition.getFirst().trim().equals("NONE")) {
                        condition = false;
                    }
                } else {
                    if(splitCondition.getFirst().trim().equals("NONE")) {
                        condition = false;
                    } else if(!splitCondition.getFirst().trim().equals("ALL")) {
                        condition = true;
                    }
                }
            }
        }
        return condition;
    }

    /**
     * Get the {@link RuleTrigger} of a {@link Rule} with the {@link Rule#getOperation()}.
     * @param operations the  List of Strings when splitting the {@link Rule#getOperation()} by ":".
     * @return the {@link RuleTrigger} of a {@link Rule}.
     */
    @SuppressWarnings("UnusedReturnValue")
    private RuleTrigger getTrigger(List<String> operations){
        if(operations.getFirst().trim().startsWith("_BEFORE_")){
            operations.set(0, operations.getFirst().replace("_BEFORE_", "").trim());
            return RuleTrigger.BEFORE;
        } else if(operations.getFirst().trim().startsWith("_ON_")){
            operations.set(0, operations.getFirst().replace("_ON_", "").trim());
            return RuleTrigger.ON;
        }
        throw new IllegalStateException("Wrong argument in triggers: rule must start with '_BEFORE_' or '_ON_'.");
    }

    /**
     * Get the {@link RuleTrigger} of a {@link Rule} with the {@link Rule#getOperation()}.
     * @param operation the {@link Rule#getOperation()}.
     * @return the {@link RuleTrigger} of a {@link Rule}.
     */
    private RuleTrigger getTrigger(String operation) {
        if(operation.trim().startsWith("_BEFORE_")) return RuleTrigger.BEFORE;
        if(operation.trim().startsWith("_ON_")) return RuleTrigger.ON;
        throw new IllegalStateException("Wrong argument in triggers: rule must start with '_BEFORE_' or '_ON_'.");
    }

    /**
     * Separate the {@link Rule} between the Comparison, the Result, if the Result is Definitive and when the {@link Rule} should Trigger.
     * @param rule {@link Rule} that is being tested.
     * @param characterStats the list of {@link CharacterStatDTO.CharacterStatSimplified CharacterStats} that hold all the edited and original {@link CharacterStat CharacterStats}.
     * @param character the {@link Character} the {@link CharacterStat CharacterStats} are linked to.
     * @param rolledValue The value Rolled.
     * @param statIDRolled ID of the {@link Stat} that was rolled.
     * @param rolledValueType Name of the {@link StatCheckpoint} corresponding to the rolled value.
     * @return a {@link ParsedRule} containing the Comparison, the Result, if the Result is Definitive and when the {@link Rule} should Trigger.
     */
    private ParsedRule parseRule(Rule rule, List<CharacterStatDTO.CharacterStatSimplified> characterStats, Character character, String rolledValueType, Integer statIDRolled, Integer rolledValue) {
        boolean definitive = false;
        String operation = rule.getOperation();
        if(operation.contains("_DEF_")) {
            operation = operation.replace("_DEF_", "");
            definitive = true;
        }

        List<String> operations = Arrays.stream(operation.split(":")).collect(Collectors.toCollection(ArrayList::new));
        getTrigger(operations);
        boolean conditionResult = checkCondition(operations.getFirst(), characterStats, character, rolledValueType, statIDRolled, rolledValue);
        return new ParsedRule(conditionResult, new ArrayList<>(Arrays.stream(operations.get(1).trim().split("(?=[=+\\-*/])|(?<=[=+\\-*/])")).map(String::strip).filter(s -> !s.isEmpty()).toList()), definitive);
    }

    /**
     * Check if a {@link Rule} should be Triggered by checking the {@link Stat Stats} and conditions in which it should trigger.
     * @param rule {@link Rule} that is being tested.
     * @param characterStats the list of {@link CharacterStatDTO.CharacterStatSimplified CharacterStats} that hold all the edited and original {@link CharacterStat CharacterStats}.
     * @param statIDRolled {@link Integer ID} of the {@link Stat} that was Rolled. {@code null} if it was just a fetch or edit of a character.
     * @param character the {@link Character} the {@link CharacterStat CharacterStats} are linked to.
     * @return {@code True} if it should Trigger, {@code False} otherwise.
     */
    private boolean ruleShouldTrigger(Rule rule, List<CharacterStatDTO.CharacterStatSimplified> characterStats, @Nullable Integer statIDRolled, Character character) {
        Pattern pattern = Pattern.compile("#(\\d+)");
        Map<Integer, CharacterStat> characterStatById = character.getCharacterStats().stream().collect(Collectors.toMap(characterStat -> characterStat.getId().getStat_id(), Function.identity()));
        List<String> operationParts = Arrays.stream(rule.getOperation().split(":")).toList();
        String operationResultPart = operationParts.getLast();
        String operationComparisonPart = operationParts.getFirst();
        for (CharacterStatDTO.CharacterStatSimplified characterStat : characterStats) {
            if(!characterStatById.containsKey(characterStat.statID())) throw new ResourceNotFoundException("Stat", characterStat.statID().toString(),"Stat with ID: '"+ characterStat.statID() + "' was not found in your campaign.");
            if(characterStatById.get(characterStat.statID()).getStat().getAuto_toggleable() && !characterStat.auto()){
                if(pattern.matcher(operationResultPart).results().anyMatch(result -> Integer.valueOf(result.group(1)).equals(characterStat.statID()))) return false;
            }
        }
        RuleTrigger trigger = getTrigger(rule.getOperation());
        if(trigger == RuleTrigger.ON && statIDRolled == null) return false;
        if(statIDRolled != null) {
            List<CharacterStatDTO.CharacterStatSimplified> foundStats = characterStats.stream().filter(characterStat -> Objects.equals(characterStat.statID(), statIDRolled)).toList();
            if(foundStats.isEmpty()) throw new ResourceNotFoundException("Stat", statIDRolled.toString());
            if(trigger == RuleTrigger.ON && pattern.matcher(operationComparisonPart).results().anyMatch(result -> Integer.valueOf(result.group(1)).equals(statIDRolled))) return true;
        }
        return trigger == RuleTrigger.BEFORE;
    }

    /**
     * Execute the Result part of a Rule, editing the stats if needed.
     * @param parsedRule the {@link ParsedRule} returned by {@link #parseRule}.
     * @param characterStats the list of {@link CharacterStatDTO.CharacterStatSimplified CharacterStats} that hold all the edited and original {@link CharacterStat CharacterStats}.
     * @param character the {@link Character} the {@link CharacterStat CharacterStats} are linked to.
     */
    private void executeResult(ParsedRule parsedRule, List<CharacterStatDTO.CharacterStatSimplified> characterStats, Character character) {
        if(parsedRule.result.isEmpty()) throw new IllegalStateException("Wrong arguments in Result: Your Rule does nothing, add a Result operation.");
        if(parsedRule.result.size() != 3) throw new IllegalStateException("Wrong arguments in Result: The result need one stat on the left (#statID), an operator (+ - * / =), a value.");
        List<CharacterStatDTO.CharacterStatSimplified> valuesOneFromRequest;
        if(parsedRule.result.getFirst().equals("ALL")){
            valuesOneFromRequest = new ArrayList<>(characterStats);
        } else {
            valuesOneFromRequest = new ArrayList<>();
            valuesOneFromRequest.add(getCharacterStat(parsedRule.result.getFirst(), characterStats, character));
        }
        if(valuesOneFromRequest.isEmpty()) throw new IllegalStateException("Wrong arguments in Result: The result need a stat on the left (#statID).");
        for(CharacterStatDTO.CharacterStatSimplified valueOneFromRequest : valuesOneFromRequest){
            String valueTwo = parseValue(parsedRule.result.getLast(), characterStats, character);
            if(valueTwo == null) valueTwo = parsedRule.result.getLast();
            CharacterStatDTO.CharacterStatSimplified finalValueOneFromRequest = valueOneFromRequest;
            CharacterStat valueOneFromStat = character.getCharacterStats().stream().filter(characterStat -> Objects.equals(characterStat.getId().getStat_id(), finalValueOneFromRequest.statID())).findFirst().orElseThrow(()-> new IllegalStateException("Wrong arguments in Result: The result need a stat on the left (#statID)."));
            if(parsedRule.result.get(1).equals("=")) {
                if(valueOneFromStat.getStat().getSelection() != null && !valueOneFromStat.getStat().getSelection().isBlank() && !CharacterStatService.parseSelection(valueOneFromStat.getStat()).contains(valueTwo)) throw new IllegalStateException("Wrong arguments in Result: The result need to be within the possible options.");
                if(valueOneFromStat.getStat().getMin() != null && valueOneFromStat.getStat().getMax() != null){
                    if(!Common.isInteger(valueTwo)) throw new IllegalStateException("Wrong arguments in Result: The result need to be a number for this Stat.");
                    int value = Integer.parseInt(valueTwo);
                    if(value < valueOneFromStat.getStat().getMin()) {
                        valueOneFromRequest = new CharacterStatDTO.CharacterStatSimplified(valueOneFromRequest, valueOneFromStat.getStat().getMin().toString());
                    } else if(value > valueOneFromStat.getStat().getMax()) {
                        valueOneFromRequest = new CharacterStatDTO.CharacterStatSimplified(valueOneFromRequest, valueOneFromStat.getStat().getMax().toString());
                    } else {
                        valueOneFromRequest = new CharacterStatDTO.CharacterStatSimplified(valueOneFromRequest, String.valueOf(value));
                    }
                } else {
                    valueOneFromRequest = new CharacterStatDTO.CharacterStatSimplified(valueOneFromRequest, valueTwo);
                }
                for (int i = 0; i < characterStats.size(); i++) {
                    if (characterStats.get(i).statID().equals(valueOneFromRequest.statID())) {
                        characterStats.set(i, new CharacterStatDTO.CharacterStatSimplified(valueOneFromRequest.statID(), valueOneFromRequest.value(), characterStats.get(i).auto()));
                        break;
                    }
                }
                if(parsedRule.definitive) {
                    valueOneFromStat.setValue(valueOneFromRequest.value());
                    characterStatService.editCharacterStatsFromRule(valueOneFromStat, character);
                }
            } else {
                if (!Common.isInteger(valueOneFromStat.getValue()) || !Common.isInteger(valueTwo) || !Common.isInteger(valueOneFromRequest.value())) throw new IllegalStateException("Wrong arguments in Result: The stats and/or values are not numerical.");
                String newValue;
                String newValueDef;
                if(parsedRule.definitive){
                    switch (parsedRule.result.get(1)){
                        case "-" -> {
                            newValueDef = String.valueOf(Integer.parseInt(valueOneFromStat.getValue()) - Integer.parseInt(valueTwo));
                            newValue = String.valueOf(Integer.parseInt(valueOneFromRequest.value()) - Integer.parseInt(valueTwo));
                        }
                        case "+" -> {
                            newValueDef = String.valueOf(Integer.parseInt(valueOneFromStat.getValue()) + Integer.parseInt(valueTwo));
                            newValue = String.valueOf(Integer.parseInt(valueOneFromRequest.value()) + Integer.parseInt(valueTwo));
                        }
                        case "*" -> {
                            newValueDef = String.valueOf(Integer.parseInt(valueOneFromStat.getValue()) * Integer.parseInt(valueTwo));
                            newValue = String.valueOf(Integer.parseInt(valueOneFromRequest.value()) * Integer.parseInt(valueTwo));
                        }
                        case "/" -> {
                            newValueDef = String.valueOf(Integer.parseInt(valueOneFromStat.getValue()) / Integer.parseInt(valueTwo));
                            newValue = String.valueOf(Integer.parseInt(valueOneFromRequest.value()) / Integer.parseInt(valueTwo));
                        }
                        default -> throw new IllegalStateException("Wrong arguments in Result: The operator can only be one of these '+ - * / ='.");
                    }
                    if(valueOneFromStat.getStat().getMin() != null && valueOneFromStat.getStat().getMax() != null){
                        if(Integer.parseInt(newValue) < valueOneFromStat.getStat().getMin()) newValue = valueOneFromStat.getStat().getMin().toString();
                        if(Integer.parseInt(newValueDef) < valueOneFromStat.getStat().getMin()) newValueDef = valueOneFromStat.getStat().getMin().toString();
                        if(Integer.parseInt(newValue) > valueOneFromStat.getStat().getMax()) newValue = valueOneFromStat.getStat().getMax().toString();
                        if(Integer.parseInt(newValueDef) > valueOneFromStat.getStat().getMax()) newValueDef = valueOneFromStat.getStat().getMax().toString();
                    }
                    if(valueOneFromStat.getStat().getSelection() != null && !valueOneFromStat.getStat().getSelection().isBlank() && !CharacterStatService.parseSelection(valueOneFromStat.getStat()).contains(newValueDef)) throw new IllegalStateException("Wrong arguments in Result: The result need to be within the possible options.");
                    valueOneFromStat.setValue(newValueDef);
                    characterStatService.editCharacterStatsFromRule(valueOneFromStat, character);
                } else {
                    switch (parsedRule.result.get(1)){
                        case "-" -> newValue = String.valueOf(Integer.parseInt(valueOneFromRequest.value()) - Integer.parseInt(valueTwo));
                        case "+" -> newValue = String.valueOf(Integer.parseInt(valueOneFromRequest.value()) + Integer.parseInt(valueTwo));
                        case "*" -> newValue = String.valueOf(Integer.parseInt(valueOneFromRequest.value()) * Integer.parseInt(valueTwo));
                        case "/" -> newValue = String.valueOf(Integer.parseInt(valueOneFromRequest.value()) / Integer.parseInt(valueTwo));
                        default -> throw new IllegalStateException("Wrong arguments in Result: The operator can only be one of these '+ - * / ='.");
                    }
                    if(valueOneFromStat.getStat().getMin() != null && valueOneFromStat.getStat().getMax() != null){
                        if(Integer.parseInt(newValue) < valueOneFromStat.getStat().getMin()) newValue = valueOneFromStat.getStat().getMin().toString();
                        if(Integer.parseInt(newValue) > valueOneFromStat.getStat().getMax()) newValue = valueOneFromStat.getStat().getMax().toString();
                    }
                }
                if(valueOneFromStat.getStat().getSelection() != null && !valueOneFromStat.getStat().getSelection().isBlank() && !CharacterStatService.parseSelection(valueOneFromStat.getStat()).contains(newValue)) throw new IllegalStateException("Wrong arguments in Result: The result need to be within the possible options.");
                for (int i = 0; i < characterStats.size(); i++) {
                    if (characterStats.get(i).statID().equals(valueOneFromRequest.statID())) {
                        characterStats.set(i, new CharacterStatDTO.CharacterStatSimplified(valueOneFromRequest.statID(), newValue, characterStats.get(i).auto()));
                        break;
                    }
                }
            }
        }
    }

    /**
     * Check a {@link Rule} and edit the {@link List<CharacterStatDTO.CharacterStatSimplified> CharacterStats} if needed.
     * @param rule {@link Rule} that is being executed.
     * @param characterStats the list of {@link CharacterStatDTO.CharacterStatSimplified CharacterStats} that hold all the edited and original {@link CharacterStat CharacterStats}.
     * @param character the {@link Character} the {@link CharacterStat CharacterStats} are linked to.
     * @param statIDRolled {@link Integer ID} of the {@link Stat} that was Rolled. {@code null} if it was just a fetch or edit of a character.
     * @param rolledValue The value Rolled.
     * @param rolledValueType Name of the {@link StatCheckpoint} corresponding to the rolled value.
     * @return {@code True} if the rule result was triggered, {@code False} otherwise.
     */
    private boolean handleRule(Rule rule, List<CharacterStatDTO.CharacterStatSimplified> characterStats, Character character, @Nullable Integer statIDRolled, @Nullable String rolledValueType, @Nullable Integer rolledValue){
        if(rule.getOperation() == null || rule.getOperation().isBlank()) return false;
        if(!ruleShouldTrigger(rule, characterStats, statIDRolled, character)) return false;
        ParsedRule parsedRule = parseRule(rule, characterStats, character, rolledValueType, statIDRolled, rolledValue);
        if(!parsedRule.condition) return false;
        executeResult(parsedRule, characterStats, character);
        return true;
    }

    /**
     * Get a {@link Character} and handle {@link Rule Rules} that could affect its {@link Stat Stats}.
     * @param character {@link Character} that needs to checks if its {@link Stat Stats} are affected.
     * @param statIDRolled {@link Integer ID} of the {@link Stat} that was Rolled. {@code null} if it was just a fetch or edit of a character.
     * @param rolledValue The value Rolled.
     * @param rolledValueType Name of the {@link StatCheckpoint} corresponding to the rolled value.
     * @param checkedRules {@link List<Rule>} If rules were already handled, triggered, and result triggered, and you don't want them checked again. If no {@link Rule Rules} send an empty List that will be populated with rules handled, triggered, and result triggered in this method.
     * @return a {@link List<CharacterStatDTO.CharacterStatResponse> List of CharacterStatResponse} with original value/value that has been definitively edited and the temporarily edited value.
     */
    public List<CharacterStatDTO.CharacterStatResponse> handleRules(Character character, List<Integer> statsNoAuto, @Nullable Integer statIDRolled, @Nullable String rolledValueType, @Nullable Integer rolledValue, @NotNull List<Rule> checkedRules) {
        Campaign campaign = character.getCampaign();
        ArrayList<Rule> rules;

        if(campaignToRuleMap.containsKey(campaign.getId())){
            if(statIDRolled == null){
                rules = campaignToRuleBEFOREOnlyMap.get(campaign.getId());
            } else {
                rules = campaignToRuleMap.get(campaign.getId());
            }
        } else {
            rules = new ArrayList<>(campaign.getRules().stream().filter(rule -> rule.getOperation() != null && !rule.getOperation().isBlank()).toList());
            campaignToRuleBEFOREOnlyMap.put(campaign.getId(), new ArrayList<>());
            campaignToRuleMap.put(campaign.getId(), new ArrayList<>());

            for (Rule rule : rules) {
                if(rule.getOperation().startsWith("_BEFORE_")){
                    campaignToRuleBEFOREOnlyMap.get(campaign.getId()).add(rule.copyWithId());
                    campaignToRuleMap.get(campaign.getId()).add(rule.copyWithId());
                } else {
                    campaignToRuleMap.get(campaign.getId()).add(rule.copyWithId());
                }
            }

            if(statIDRolled != null){
                campaignToRuleBEFOREOnlyMap.get(campaign.getId()).sort(Comparator.comparing(Rule::getPriority));
                rules = new ArrayList<>(campaignToRuleBEFOREOnlyMap.get(campaign.getId()));
            } else {
                campaignToRuleMap.get(campaign.getId()).sort(Comparator.comparing(Rule::getPriority));
                rules = new ArrayList<>(campaignToRuleMap.get(campaign.getId()));
            }
        }

        if(!checkedRules.isEmpty()) {
            rules.removeIf(rule -> checkedRules.stream().anyMatch(checkedRule -> Objects.equals(checkedRule.getId(), rule.getId())));
        }

        List<CharacterStatDTO.CharacterStatSimplified> characterStats = new ArrayList<>(character.getCharacterStats().stream().map(characterStat-> new CharacterStatDTO.CharacterStatSimplified(characterStat.getStat().getId(), characterStat.getValue(), !statsNoAuto.contains(characterStat.getStat().getId()))).toList());
        //
        //!!!PUT ITEMS STAT EDIT HERE!!!
        //
        for (Rule rule : rules) {
            if(handleRule(rule, characterStats, character, statIDRolled, rolledValueType, rolledValue)) checkedRules.add(rule);
        }
        Map<Integer, CharacterStat> realCharacterStats = character.getCharacterStats().stream().collect(Collectors.toMap(realCharacterStat->realCharacterStat.getId().getStat_id(), Function.identity()));
        Map<Integer, CharacterStat> originalCharacterStats = character.getCharacterStats().stream().collect(Collectors.toMap(originalCharacterStat->originalCharacterStat.getId().getStat_id(), Function.identity()));
        ArrayList<CharacterStatDTO.CharacterStatResponse> characterStatsToReturn = new ArrayList<>();
        for (CharacterStatDTO.CharacterStatSimplified characterStat : characterStats) {
            if(Objects.equals(realCharacterStats.get(characterStat.statID()).getValue(), characterStat.value())){
                characterStatsToReturn.add(new CharacterStatDTO.CharacterStatResponse(new StatDTO.StatResponse(originalCharacterStats.get(characterStat.statID()).getStat()), characterStat.value(), null));
            } else if(Objects.equals(originalCharacterStats.get(characterStat.statID()).getValue(), characterStat.value())){
                characterStatsToReturn.add(new CharacterStatDTO.CharacterStatResponse(originalCharacterStats.get(characterStat.statID()), null));
            } else {
                characterStatsToReturn.add(new CharacterStatDTO.CharacterStatResponse(originalCharacterStats.get(characterStat.statID()), characterStat.value()));
            }
        }
        return characterStatsToReturn;
    }

    //Put in Commentary while the creation of rules is not made by users.
//    /**
//     * Test a {@link Rule} by creating a fake {@link Character} with all {@link Stat Stats} and triggering the {@link Rule} with all {@link Stat Stats} as stat rolled and then with no stat roll just a normal fetch.
//     * @param rule {@link Rule} to test.
//     * @param campaign The {@link Campaign} it was originally created from with related {@link Stat Stats}.
//     */
//    public void testNewRule(Rule rule, Campaign campaign) {
//        Rule testedRule = new Rule();
//        testedRule.setOperation(rule.getOperation().replace("_DEF_", ""));
//        testedRule.setStats(rule.getStats());
//        Character character = new Character();
//        character.setCampaign(campaign);
//        Set<CharacterStat> characterStats = new HashSet<>();
//        List<Stat> statsInCampaign = campaign.getRules().stream().flatMap(ruleInMapping -> ruleInMapping.getStats().stream()).distinct().collect(Collectors.toCollection(ArrayList::new));
//        for (Stat stat : statsInCampaign) {
//            String value;
//            if(stat.getSelection() != null && !stat.getSelection().isBlank()) {
//                if(stat.getDefaultValue()!=null) {
//                    value = stat.getDefaultValue();
//                } else {
//                    value = CharacterStatService.parseSelection(stat).getFirst();
//                }
//            } else if(stat.getDefaultValue() != null && !stat.getDefaultValue().isBlank()) {
//                value = stat.getDefaultValue();
//            } else {
//                if(stat.getMin() != null && stat.getMax() != null) {
//                    value = String.valueOf((stat.getMin() + stat.getMax()) / 2);
//                } else {
//                    value = "2";
//                }
//            }
//            characterStats.add(new CharacterStat(new CharacterStatId(1, stat.getId()), character, stat, value));
//        }
//        character.setCharacterStats(characterStats);
//        ArrayList<CharacterStatDTO.CharacterStatSimplified> statsToSend = new ArrayList<>(characterStats.stream().map(stat1 -> new CharacterStatDTO.CharacterStatSimplified(stat1.getId().getStat_id(), stat1.getValue(), stat1.getStat().getAuto_toggleable())).toList());
//        for (Stat stat : statsInCampaign) {
//            if(rule.getStats().stream().anyMatch(ruleStat -> Objects.equals(ruleStat.getId(), stat.getId()))){
//                for (int i = 0; i < stat.getRoll(); i++) {
//                    handleRule(testedRule, statsToSend, character, stat.getId(), rollService.getRolledValueSuccessName(i,), i);
//                }
//
//            }
//        }
//        handleRule(testedRule, statsToSend, character, null, null, null);
//    }
}
