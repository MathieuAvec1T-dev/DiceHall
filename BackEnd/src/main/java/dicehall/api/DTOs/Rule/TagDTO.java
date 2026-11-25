package dicehall.api.DTOs.Rule;

import dicehall.api.Entities.Rule.Tag;
import dicehall.api.Entities.Rule.Type;

import java.util.List;

public class TagDTO {
    public record TagResponse(Integer id, String name, Type type, List<RuleDTO.RuleForTagResponse> rules){
        public TagResponse(Tag tag) {
            this(tag.getId(), tag.getName(), tag.getType(), tag.getRules().stream().map(RuleDTO.RuleForTagResponse::new).toList());
        }
    }
    public record TagForRuleResponse(Integer id, String name, Type type){
        public TagForRuleResponse(Tag tag) {
            this(tag.getId(), tag.getName(), tag.getType());
        }
    }
}
