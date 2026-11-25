package dicehall.api.Controllers;

import dicehall.api.DTOs.Character.CharacterDTO;
import dicehall.api.DTOs.Character.CharacterStatDTO;
import dicehall.api.Security.Exceptions.ErrorResponse;
import dicehall.api.Services.Character.CharacterService;
import dicehall.api.Services.Character.RollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@RequestMapping("/characters")
public class CharacterController {

    private final CharacterService characterService;
    private final RollService rollService;

    public CharacterController(CharacterService characterService, RollService rollService) {
        this.characterService = characterService;
        this.rollService = rollService;
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Character was found.", content = @Content(schema = @Schema(implementation = CharacterDTO.CharacterResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access Denied.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "User Not part of Campaign example.", value = "{\"status\": 403, \"message\": \"You are not part of the Campaign\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "404", description = "Entity not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Stat not found by ID example.", value = "{\"status\": 404, \"message\": \"Stat was not found with value: 26\nStat with ID: '26' was not found in your campaign.\", \"timestamp\": \"2026-01-15T10:30:00\"}"), @ExampleObject(name = "Character not found by ID example.", value = "{\"status\": 404, \"message\": \"Character was not found with value: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "409", description = "Illegal argument.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Error with a Rule.", value = "{\"status\": 400, \"message\": \"Wrong argument in X: Explanation.\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example.", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Find a Character by ID.", description = "Returns the character's public data.")
    @GetMapping("/{characterID}")
    public CharacterDTO.CharacterResponse findCharacter(@PathVariable Integer characterID, @RequestHeader String authorization){
        return characterService.findById(characterID, authorization);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Character was created.", content = @Content(schema = @Schema(implementation = CharacterDTO.CharacterResponse.class))),
            @ApiResponse(responseCode = "404", description = "Entity not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Campaign not found by ID example.", value = "{\"status\": 404, \"message\": \"Campaign was not found with value: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}"),@ExampleObject(name = "User not found by ID example.", value = "{\"status\": 404, \"message\": \"User was not found with value: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "403", description = "Access Denied.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "User Not part of Campaign example.", value = "{\"status\": 403, \"message\": \"You are not part of the Campaign\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "409", description = "Illegal argument.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                    @ExampleObject(name = "Selected User not part of Campaign example.", value = "{\"status\": 400, \"message\": \"The selected User is not part of the Campaign.\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "Missing required Stats example.", value = "{\"status\": 400, \"message\": \"Required Stats were not supplied: [Stat1, Stat2].\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "Missing a required Value in a selected Stat example.", value = "{\"status\": 400, \"message\": \"Value required for stat: Stat1.\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "Stat should be a number example.", value = "{\"status\": 400, \"message\": \"Stat \"Stat1\" should be a number.\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "Stat inferior or superior to what is allowed example.", value = "{\"status\": 400, \"message\": \"Stat \"Stat1\" is inferior to minimum/maximum value \"80\".\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "Value of selectable Stat not in list example.", value = "{\"status\": 400, \"message\": \"Value 'badValue' isn't part of the available options.\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "One of three name field needed example.", value = "{\"status\": 400, \"message\": \"Need at least one of the three name fields.\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "Error with a Rule.", value = "{\"status\": 400, \"message\": \"Wrong argument in X: Explanation.\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example.", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Create a Character.", description = "Returns the character's public data.")
    @PostMapping
    public CharacterDTO.CharacterResponse createCharacter(@Valid @RequestBody CharacterDTO.CharacterCreateRequest request, @RequestHeader String authorization){
        return characterService.create(request,authorization);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Character was edited.", content = @Content(schema = @Schema(implementation = CharacterDTO.CharacterResponse.class))),
            @ApiResponse(responseCode = "404", description = "Entity not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                    @ExampleObject(name = "Campaign not found by ID example.", value = "{\"status\": 404, \"message\": \"Campaign was not found with value: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "Character not found by ID example.", value = "{\"status\": 404, \"message\": \"Character was not found with value: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "User not found by ID example.", value = "{\"status\": 404, \"message\": \"User was not found with value: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "403", description = "Access Denied.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "User Not part of Campaign example.", value = "{\"status\": 403, \"message\": \"You are not part of the Campaign\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "409", description = "Illegal argument.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                    @ExampleObject(name = "Selected User not part of Campaign example.", value = "{\"status\": 400, \"message\": \"The selected User is not part of the Campaign.\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "Missing required Stats example.", value = "{\"status\": 400, \"message\": \"Required Stats were not supplied: [Stat1, Stat2].\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "Missing a required Value in a selected Stat example.", value = "{\"status\": 400, \"message\": \"Value required for stat: Stat1.\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "Stat should be a number example.", value = "{\"status\": 400, \"message\": \"Stat \"Stat1\" should be a number.\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "Stat inferior or superior to what is allowed example.", value = "{\"status\": 400, \"message\": \"Stat \"Stat1\" is inferior to minimum/maximum value \"80\".\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "Value of selectable Stat not in list example.", value = "{\"status\": 400, \"message\": \"Value 'badValue' isn't part of the available options.\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "One of three name field needed example.", value = "{\"status\": 400, \"message\": \"Need at least one of the three name fields.\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "Error with a Rule.", value = "{\"status\": 400, \"message\": \"Wrong argument in X: Explanation.\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example.", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Edit a Character.", description = "Returns the character's public data.")
    @PostMapping("/edit")
    public CharacterDTO.CharacterResponse editCharacter(@Valid @RequestBody CharacterDTO.CharacterEditRequest request, @RequestHeader String authorization){
        return characterService.edit(request, authorization);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Character was validated.", content = @Content(schema = @Schema(implementation = CharacterDTO.CharacterResponse.class))),
            @ApiResponse(responseCode = "404", description = "Entity not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                    @ExampleObject(name = "Campaign not found by ID example.", value = "{\"status\": 404, \"message\": \"Campaign was not found with value: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "Character not found by ID example.", value = "{\"status\": 404, \"message\": \"Character was not found with value: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}"),})),
            @ApiResponse(responseCode = "403", description = "Access Denied.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "User Not part of Campaign or not Admin or Owner example.", value = "{\"status\": 403, \"message\": \"You can't validate a character if you are not the Owner or an Admin of the Campaign.\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example.", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Validate a Character.", description = "Returns the character's public data. Return 409 if user does not have permissions to Validate.")
    @PostMapping("/validate")
    public CharacterDTO.CharacterResponse validateCharacter(@Valid @RequestBody CharacterDTO.CharacterValidateRequest request, @RequestHeader String authorization){
        return characterService.validate(request, authorization);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roll was done.", content = @Content(schema = @Schema(implementation = CharacterDTO.CharacterRollResponse.class))),
            @ApiResponse(responseCode = "404", description = "Entity not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {
                    @ExampleObject(name = "Stat not found by ID example.", value = "{\"status\": 404, \"message\": \"Stat was not found with value: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}"),
                    @ExampleObject(name = "Character not found by ID example.", value = "{\"status\": 404, \"message\": \"Character was not found with value: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}"),})),
            @ApiResponse(responseCode = "409", description = "Illegal argument.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Error with a Rule.", value = "{\"status\": 400, \"message\": \"Wrong argument in X: Explanation.\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "403", description = "Access Denied.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "User not owner of Character example.", value = "{\"status\": 403, \"message\": \"You need to be the Character's Owner to roll a dice on his Stats.\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example.", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Roll a dice on a Stat.", description = "Returns the character's public data and roll result.")
    @PostMapping("/roll")
    public CharacterDTO.CharacterRollResponse roll(@Valid @RequestBody CharacterStatDTO.CharacterStatRollRequest request, @RequestHeader String authorization) {
        return rollService.rollStat(request, authorization);
    }
}
