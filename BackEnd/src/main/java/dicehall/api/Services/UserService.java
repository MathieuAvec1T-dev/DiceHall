package dicehall.api.Services;

import dicehall.api.DTOs.UserDTO;
import dicehall.api.Entities.Token;
import dicehall.api.Entities.User;
import dicehall.api.Repositories.UserRepository;
import dicehall.api.Security.Exceptions.UserNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    /**
     * Find a {@link User} by its {@link Integer ID} and return a {@link dicehall.api.DTOs.UserDTO.UserResponse UserResponse}.
     * @param id of the user.
     * @return {@link dicehall.api.DTOs.UserDTO.UserResponse UserResponse} of the corresponding {@link User}
     */
    public UserDTO.UserResponse findById(Integer id) {
        return new UserDTO.UserResponse(userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
    }
    /**
     * Find a {@link User} by its {@link String Username} and return a {@link List<dicehall.api.DTOs.UserDTO.UserResponse> List of UserResponse}.
     * @param username of the user.
     * @return {@link List<dicehall.api.DTOs.UserDTO.UserResponse> List of UserResponse} of the corresponding {@link User Users}
     */
    public List<UserDTO.UserResponse> findByUsername(String username) {
        List<UserDTO.UserResponse> userResponses = userRepository.findAllByUsername(username).stream().map(UserDTO.UserResponse::new).toList();
        if(userResponses.isEmpty()) throw new UserNotFoundException(username);
        return userResponses;
    }

    /**
     * Create a {@link User} with the passed information through {@link UserDTO.UserCreateRequest UserCreateRequest}.
     * @param request {@link dicehall.api.DTOs.UserDTO.UserCreateRequest UserCreateRequest} containing {@code username, email, password}.
     * @return {@link dicehall.api.DTOs.UserDTO.UserAuthResponse UserAuthResponse} of the created user containing the token.
     */
    public UserDTO.UserAuthResponse createUser(UserDTO.UserCreateRequest request){
        if(userRepository.existsByEmail(request.email())) throw new IllegalStateException("Email already taken.");
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user = userRepository.save(user);
        return new UserDTO.UserAuthResponse(tokenService.createToken(user));
    }

    /**
     * Authenticate a {@link User} with the passed information through {@link UserDTO.UserAuthRequest UserAuthRequest}.
     * @param request {@link dicehall.api.DTOs.UserDTO.UserAuthRequest UserAuthRequest} containing {@code email, password}.
     * @return {@link dicehall.api.DTOs.UserDTO.UserAuthResponse UserAuthResponse} of the authenticated user containing the token.
     */
    public UserDTO.UserAuthResponse authUser(UserDTO.UserAuthRequest request){
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new UserNotFoundException(request.email()));
        if(!passwordEncoder.matches(request.password(), user.getPassword())) throw new IllegalStateException("Incorrect password.");
        return new UserDTO.UserAuthResponse(tokenService.updateToken(user));
    }

    /**
     * Authenticate a {@link User} with the passed {@link Token}.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @param id of the {@link User}.
     * @return {@link dicehall.api.DTOs.UserDTO.UserResponse UserResponse} of the authenticated user.
     */
    public UserDTO.UserResponse validate(String authorization, Integer id) {
        authorization = authorization.replace("Bearer ", "");
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        if(user.getToken() == null || !tokenService.isTokenValid(id,authorization)) throw new BadCredentialsException("Invalid token: " + authorization);
        return new UserDTO.UserResponse(userRepository.save(user));
    }

    /**
     * Update a {@link User} with the passed information through {@link UserDTO.UserEditRequest UserEditRequest}.
     * User should be connected and the same as the passed ID.
     * @param request {@link dicehall.api.DTOs.UserDTO.UserEditRequest UserEditRequest} containing {@code id, username}.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return {@link dicehall.api.DTOs.UserDTO.UserResponse UserResponse} of the updated user.
     */
    public UserDTO.UserResponse updateUser(UserDTO.UserEditRequest request, String authorization){
        authorization = authorization.replace("Bearer ", "");
        User user = userRepository.findById(request.id()).orElseThrow(() -> new UserNotFoundException(request.id()));
        if(!tokenService.isTokenValid(user.getId(), authorization)) throw new BadCredentialsException("Invalid token: " + authorization);
        user.setUsername(request.username());
        return new UserDTO.UserResponse(userRepository.save(user));
    }
}
