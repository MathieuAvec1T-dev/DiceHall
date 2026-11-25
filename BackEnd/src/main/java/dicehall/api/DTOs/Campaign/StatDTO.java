package dicehall.api.DTOs.Campaign;

import dicehall.api.DTOs.Rule.RuleDTO;
import dicehall.api.DTOs.Rule.TagDTO;
import dicehall.api.Entities.Campaign.Stat;
import dicehall.api.Services.Character.CharacterStatService;

import java.util.List;

public class StatDTO {
    public record StatResponse(Integer id, String name, Boolean required, List<String> selection, Integer min, Integer max, Integer dice, Boolean auto_toggleable, List<RuleDTO.RuleResponse> rules, List<TagDTO.TagForRuleResponse> tags){
        public StatResponse(Stat stat){
            this(stat.getId(), stat.getName(), stat.getRequired(), CharacterStatService.parseSelection(stat), stat.getMin(), stat.getMax(), stat.getRoll(), stat.getAuto_toggleable(), stat.getRules().stream().map(RuleDTO.RuleResponse::new).toList(), stat.getRules().stream().flatMap(rule -> rule.getTags().stream()).distinct().map(TagDTO.TagForRuleResponse::new).toList());
        }
    }
}
