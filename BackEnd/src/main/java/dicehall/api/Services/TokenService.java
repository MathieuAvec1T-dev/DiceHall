package dicehall.api.Services;

import dicehall.api.Entities.Token;
import dicehall.api.Entities.User;
import dicehall.api.Repositories.TokenRepository;
import dicehall.api.Repositories.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;

@Service
public class TokenService {
    private final JwtEncoder jwtEncoder;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    public TokenService(JwtEncoder jwtEncoder, TokenRepository tokenRepository, UserRepository userRepository) {
        this.jwtEncoder = jwtEncoder;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    /**
     * Check if a {@link Token} is valid.
     * @param id of the {@link User} trying to validate it's token.
     * @param tokenValue {@link Token} value sent by {@link User}.
     * @return {@code True} if the {@link Token} value correspond to the one in the {@link User} found with the ID, {@code False} otherwise.
     */
    public boolean isTokenValid(Integer id, String tokenValue){
        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty()) return false;
        return Objects.equals(tokenValue, user.get().getToken().getValue());
    }

    /**
     * Get a {@link User} by its {@link Token}.
     * @param tokenValue {@link Token} value sent by {@link User}.
     * @return the {@link User} corresponding to the {@link Token}.
     */
    public User getUserByToken(String tokenValue){
        return tokenRepository.findByValue(tokenValue).orElseThrow(()-> new BadCredentialsException("Invalid token: " + tokenValue)).getUser();
    }

    /**
     * Create a {@link Token} for a {@link User}.
     * @param user {@link User} needing the creation.
     * @return the Updated {@link User} with its new {@link Token}.
     */
    public User createToken(User user){
        if(tokenRepository.findByUser(user).isPresent()) return updateToken(user);
        String jwtValue = newTokenValue(user);
        Token token = new Token();
        token.setUser(user);
        token.setValue(jwtValue);
        user.setToken(token);
        userRepository.save(user);
        return user;
    }

    /**
     * Update the {@link Token} of a {@link User}.
     * @param user {@link User} needing the update.
     * @return the Updated {@link User} with its new {@link Token}.
     */
    public User updateToken(User user) {
        if(tokenRepository.findByUser(user).isEmpty()) return createToken(user);
        String jwtValue = newTokenValue(user);
        tokenRepository.delete(tokenRepository.findByUser(user).get());
        Token token = new Token();
        token.setValue(jwtValue);
        token.setUser(user);
        user.setToken(token);
        userRepository.save(user);
        return user;
    }

    /**
     * Creates a new {@link Token} value.
     * @param user {@link User} needing a new token.
     * @return the {@link Token} value.
     */
    private String newTokenValue(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claimsSet = JwtClaimsSet.builder().issuer("DiceHall").subject(String.valueOf(user.getId())).issuedAt(now).expiresAt(now.plus(365, ChronoUnit.DAYS)).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claimsSet)).getTokenValue();
    }
}
