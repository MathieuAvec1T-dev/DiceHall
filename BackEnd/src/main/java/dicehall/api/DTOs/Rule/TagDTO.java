package dicehall.api.DTOs.Rule;

import dicehall.api.Entities.Rule.Tag;

import java.util.List;

public class TagDTO {
    public record TagResponse(Integer id, String name, List<RuleDTO.RuleForTagResponse> rules){
        public TagResponse(Tag tag) {
            this(tag.getId(), tag.getName(), tag.getRules().stream().map(RuleDTO.RuleForTagResponse::new).toList());
        }
    }
    public record TagForRuleResponse(Integer id, String name){
        public TagForRuleResponse(Tag tag) {
            this(tag.getId(), tag.getName());
        }
    }
}
