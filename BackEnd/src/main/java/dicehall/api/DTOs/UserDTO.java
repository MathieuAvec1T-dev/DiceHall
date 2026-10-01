package dicehall.api.DTOs;

import dicehall.api.Entities.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDTO {
    public record UserResponse(Integer id,@NotBlank @Size(min = 3, max = 64)  String username, @NotBlank @Size(min = 3, max = 64) @Email String email) {
        public UserResponse(User user) {
            this(user.getId(), user.getUsername(), user.getEmail());
        }
    }
    public record UserAuthResponse(Integer id,@NotBlank @Size(min = 3, max = 64)  String username, @NotBlank @Size(min = 3, max = 64) @Email String email, TokenDTO.TokenAuthResponse token) {
        public UserAuthResponse(User user) {
            this(user.getId(), user.getUsername(), user.getEmail(), new TokenDTO.TokenAuthResponse(user.getToken()));
        }
    }


    public record UserCreateRequest(@NotBlank @Size(min = 3, max = 64) String username, @NotBlank @Size(min = 3, max = 64) @Email String email, @NotBlank @Size(min = 8, max = 64) String password) {}
    public record UserEditRequest(Integer id, @NotBlank @Size(min = 3, max = 64) String username) {}
    public record UserAuthRequest(@NotBlank @Size(min = 3, max = 64) @Email String email, @NotBlank @Size(min = 8, max = 64) String password) {}
}
