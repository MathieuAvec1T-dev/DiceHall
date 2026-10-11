package dicehall.api.DTOs.User;

import dicehall.api.Entities.User.Token;

import java.time.LocalDateTime;

public class TokenDTO {
    public record TokenAuthResponse(String value, LocalDateTime createdAt){
        public TokenAuthResponse(Token token) {
            this(token.getValue(), token.getCreatedAt());
        }
    }
}
