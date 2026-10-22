package dicehall.api.DTOs.Campaign;

import dicehall.api.DTOs.Rule.RuleDTO;
import dicehall.api.Entities.Campaign.Stat;
import dicehall.api.Services.Character.CharacterService;

import java.util.List;

public class StatDTO {
    public record StatResponse(Integer id, String name, Boolean required, List<String> selection, Integer min, Integer max , List<RuleDTO.RuleResponse> rules){
        public StatResponse(Stat stat){
            this(stat.getId(), stat.getName(), stat.getRequired(), CharacterService.parseSelection(stat), stat.getMin(), stat.getMax(), stat.getRules().stream().map(RuleDTO.RuleResponse::new).toList());
        }
    }
}
