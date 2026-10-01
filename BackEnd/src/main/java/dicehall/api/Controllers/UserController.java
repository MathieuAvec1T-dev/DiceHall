package dicehall.api.Controllers;

import dicehall.api.DTOs.UserDTO;
import dicehall.api.Security.Exceptions.ErrorResponse;
import dicehall.api.Services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User was found.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDTO.UserResponse.class)))),
            @ApiResponse(responseCode = "404", description = "User not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "User not found by Username example", value = "{\"status\": 404, \"message\": \"User not found: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @Operation(summary = "Get a user by ID", description = "Returns a single user's public data, excluding the password and token. Returns 404 if no user exists with that ID.")
    @GetMapping("/id/{id}")
    public UserDTO.UserResponse getById(@PathVariable Integer id) {
        return userService.findById(id);
    }
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User(s) were found.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDTO.UserResponse.class)))),
            @ApiResponse(responseCode = "404", description = "User(s) not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "User not found by Username example", value = "{\"status\": 404, \"message\": \"User not found: badUsername\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @Operation(summary = "Get users by Username", description = "Returns a list of user's public data, excluding the password and token. Returns 404 if no user exists with that username.")
    @GetMapping("/username/{username}")
    public List<UserDTO.UserResponse> getByUsername(@PathVariable String username) {
        return userService.findByUsername(username);
    }

    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User was authenticated.", content = @Content(schema = @Schema(implementation = UserDTO.UserAuthResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "User not found by Email example", value = "{\"status\": 404, \"message\": \"User not found: bademail@gmail.com\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @Operation(summary = "Authenticate as a user.", description = "Returns the user's public data including it's token, excluding the password. Returns 404 if no user exists with that Email.")
    @PostMapping("/auth/login")
    public UserDTO.UserAuthResponse auth(@Valid @RequestBody UserDTO.UserAuthRequest user) {
        return userService.authUser(user);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User was authenticated.", content = @Content(schema = @Schema(implementation = UserDTO.UserAuthResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "User not found by Email example", value = "{\"status\": 404, \"message\": \"User not found: bademail@gmail.com\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @Operation(summary = "Authenticate as a user.", description = "Returns the user's public data including it's token, excluding the password. Returns 404 if no user exists with that Email.")
    @PostMapping("/auth/validate/{id}")
    public UserDTO.UserResponse auth(@RequestHeader("Authorization") String authorization, @PathVariable Integer id) {
        return userService.validate(authorization, id);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User was created and authenticated.", content = @Content(schema = @Schema(implementation = UserDTO.UserAuthResponse.class))),
            @ApiResponse(responseCode = "409", description = "Illegal argument.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Email already used.", value = "{\"status\": 409, \"message\": \"Email already taken.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @Operation(summary = "Create a user.", description = "Returns the user's public data including it's token, excluding the password. Returns 409 if the Email was already used.")
    @PostMapping
    public UserDTO.UserAuthResponse create(@Valid @RequestBody UserDTO.UserCreateRequest user) {
        return userService.createUser(user);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Username of User was updated.", content = @Content(schema = @Schema(implementation = UserDTO.UserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "User not found by ID example", value = "{\"status\": 404, \"message\": \"User not found: 42\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @Operation(summary = "Update a user.", description = "Returns the user's new public data, excluding the password and token. Returns 404 if no user exists with that ID. Returns 401 if the token is invalid.")
    @PostMapping("/edit")
    public UserDTO.UserResponse edit(@Valid @RequestBody UserDTO.UserEditRequest user, @RequestHeader("Authorization") String authorization) {
        return userService.updateUser(user, authorization);
    }
}
