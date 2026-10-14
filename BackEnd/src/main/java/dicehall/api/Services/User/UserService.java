package dicehall.api.Services.User;

import dicehall.api.DTOs.User.UserDTO;
import dicehall.api.Entities.User.Token;
import dicehall.api.Entities.User.User;
import dicehall.api.Repositories.User.TokenRepository;
import dicehall.api.Repositories.User.UserRepository;
import dicehall.api.Security.Exceptions.UserNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final TokenRepository tokenRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.tokenRepository = tokenRepository;
    }

    /**
     * Find a {@link User} by its {@link Integer ID} and return a {@link UserDTO.UserResponse UserResponse}.
     * @param id of the user.
     * @return {@link UserDTO.UserResponse UserResponse} of the corresponding {@link User}
     */
    public UserDTO.UserResponse findById(Integer id) {
        return new UserDTO.UserResponse(userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id)));
    }
    /**
     * Find a {@link User} by its {@link String Username} and return a {@link List<UserDTO.UserResponse> List of UserResponse}.
     * @param username of the user.
     * @param pageable {@link Pageable} information to send to request.
     * @return {@link List<UserDTO.UserResponse> List of UserResponse} of the corresponding {@link User Users}
     */
    public List<UserDTO.UserResponse> findByUsername(String username, Pageable pageable) {
        return userRepository.findAllByUsername(username, pageable).stream().map(UserDTO.UserResponse::new).toList();
    }
    /**
     * Find a {@link User} by its {@link String Email} and return a {@link List<UserDTO.UserResponse> List of UserResponse}.
     * @param email of the user.
     * @param pageable {@link Pageable} information to send to request.
     * @return {@link List<UserDTO.UserResponse> List of UserResponse} of the corresponding {@link User Users}
     */
    public List<UserDTO.UserResponse> findByEmail(String email, Pageable pageable) {
        return userRepository.findByEmailContainingIgnoreCase(email, pageable).stream().map(UserDTO.UserResponse::new).toList();
    }

    /**
     * Create a {@link User} with the passed information through {@link UserDTO.UserCreateRequest UserCreateRequest}.
     * @param request {@link UserDTO.UserCreateRequest UserCreateRequest} containing {@code username, email, password}.
     * @return {@link UserDTO.UserAuthResponse UserAuthResponse} of the created user containing the token.
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
     * Disconnect a {@link User} by removing it's related {@link Token}.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return A validation message.
     */
    public String disconnect(String authorization) {
        tokenRepository.removeTokenByUser(validate(authorization));
        return "Disconnected successfully";
    }

    /**
     * Authenticate a {@link User} with the passed information through {@link UserDTO.UserAuthRequest UserAuthRequest}.
     * @param request {@link UserDTO.UserAuthRequest UserAuthRequest} containing {@code email, password}.
     * @return {@link UserDTO.UserAuthResponse UserAuthResponse} of the authenticated user containing the token.
     */
    public UserDTO.UserAuthResponse authUser(UserDTO.UserAuthRequest request){
        User user = userRepository.findByEmail(request.email()).orElseThrow(() -> new UserNotFoundException(request.email()));
        if(!passwordEncoder.matches(request.password(), user.getPassword())) throw new IllegalStateException("Incorrect password.");
        return new UserDTO.UserAuthResponse(tokenService.updateToken(user));
    }

    /**
     * Authenticate a {@link User} with the passed {@link Token}.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return {@link UserDTO.UserResponse UserResponse} of the authenticated user.
     */
    public UserDTO.UserResponse authUser(String authorization) {
        return new UserDTO.UserResponse(validate(authorization));
    }

    /**
     * Validate if authorization credentials sent by user are valid.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return {@link User} corresponding.
     */
    public User validate(String authorization) {
        authorization = authorization.replace("Bearer ", "");
        return tokenService.findUserByToken(authorization);
    }

    /**
     * Update a {@link User} with the passed information through {@link UserDTO.UserEditRequest UserEditRequest}.
     * User should be connected.
     * @param request {@link UserDTO.UserEditRequest UserEditRequest} containing {@code id, username}.
     * @param authorization The {@code Authorization} Header containing {@code Bearer [}{@link Token TOKEN}{@code ]}.
     * @return {@link UserDTO.UserResponse UserResponse} of the updated user.
     */
    public UserDTO.UserResponse updateUser(UserDTO.UserEditRequest request, String authorization){
        User user = validate(authorization);
        if(request.username() != null && !request.username().isEmpty()) user.setUsername(request.username());
        if(request.email() != null && !request.email().isEmpty()) user.setEmail(request.email());
        if(request.password() != null && !request.password().isEmpty()) user.setPassword(passwordEncoder.encode(request.password()));
        return new UserDTO.UserResponse(userRepository.save(user));
    }
}
