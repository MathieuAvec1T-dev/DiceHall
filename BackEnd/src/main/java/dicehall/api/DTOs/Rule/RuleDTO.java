package dicehall.api.DTOs.Rule;

import dicehall.api.Entities.Rule.Rule;

import java.util.List;

public class RuleDTO {
    public record RuleResponse(Integer id, String name, String description, List<TagDTO.TagForRuleResponse> tags){
        public RuleResponse(Rule rule) {
            this(rule.getId(), rule.getName(), rule.getDescription(), rule.getTags().stream().map(TagDTO.TagForRuleResponse::new).toList());
        }
    }
    public record  RuleForTagResponse(Integer id, String name, String description){
        public RuleForTagResponse(Rule rule) {
            this(rule.getId(), rule.getName(), rule.getDescription());
        }
    }
}
