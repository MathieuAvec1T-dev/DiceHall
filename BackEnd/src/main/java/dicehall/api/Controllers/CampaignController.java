package dicehall.api.Controllers;

import dicehall.api.DTOs.Campaign.CampaignDTO;
import dicehall.api.DTOs.Rule.RuleDTO;
import dicehall.api.DTOs.Rule.TagDTO;
import dicehall.api.Security.Exceptions.ErrorResponse;
import dicehall.api.Services.Campaign.CampaignService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@RequestMapping("/campaigns")
public class CampaignController {
    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Campaign was found.", content = @Content(schema = @Schema(implementation = CampaignDTO.CampaignResponse.class))),
            @ApiResponse(responseCode = "404", description = "Campaign not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Campaign not found by ID example.", value = "{\"status\": 404, \"message\": \"Campaign was not found with value: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example.", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Get a campaign by ID.", description = "Returns a single campaign's public data. Need the \"Authorization\" token if campaign is private. Returns 404 if no campaign exists with that ID.")
    @GetMapping("/id/{id}")
    public CampaignDTO.CampaignResponse getById(@PathVariable Integer id, @RequestHeader(value = "Authorization", required = false) String authorization) {
        return campaignService.findById(id, authorization);
    }
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Campaign(s) were found.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CampaignDTO.CampaignResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example.", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Get a campaign by User ID.", description = "Returns a List of campaigns's public data. Need the \"Authorization\" token if private campaigns are asked. Returns 404 if no campaign exists with these parameters .")
    @GetMapping("/user/{id}")
    public List<CampaignDTO.CampaignResponse> getByUserId(@PathVariable Integer id, @RequestParam(required = false) Boolean addPrivate, @RequestHeader(value = "Authorization", required = false) String authorization) {
        return campaignService.findByUserId(id, addPrivate != null && addPrivate, authorization);
    }
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Campaign(s) were found.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CampaignDTO.CampaignResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example.", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Get a campaign by Owner user ID.", description = "Returns a List of campaigns's public data. Need the \"Authorization\" token if private campaigns are asked. Returns 404 if no campaign exists with these parameters .")
    @GetMapping("/owner/{id}")
    public List<CampaignDTO.CampaignResponse> getByOwnerId(@PathVariable Integer id, @RequestParam(required = false) Boolean addPrivate, @RequestHeader(value = "Authorization", required = false) String authorization) {
        return campaignService.findByOwnerId(id, addPrivate != null && addPrivate, authorization);
    }
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Campaign(s) were found.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CampaignDTO.CampaignResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example.", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Get a campaign by Name search.", description = "Returns a List of campaigns's public data. Need the \"Authorization\" token if private campaigns are asked. Returns 404 if no campaign has a name containing the searched String.")
    @GetMapping("/name/{searchParam}")
    public List<CampaignDTO.CampaignResponse> getByCampaignName(@PathVariable String searchParam, @RequestParam(required = false) Pageable pageable, @RequestParam(required = false) Boolean addPrivate, @RequestHeader(value = "Authorization", required = false) String authorization) {
        return campaignService.findIfNameContains(searchParam, pageable, addPrivate != null && addPrivate, authorization);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rule(s) were found.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = RuleDTO.RuleResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Campaign not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Campaign not found by ID example.", value = "{\"status\": 404, \"message\": \"Campaign was not found with value: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example.", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Get Rules from a Campaign ID.", description = "Returns the rules's public data. Need the \"Authorization\" token if private campaigns are asked. Returns 404 if no campaign with this ID exists.")
    @GetMapping("/rules/{campaignID}")
    public List<RuleDTO.RuleResponse> GetRules(@PathVariable Integer campaignID, @Nullable Pageable pageable, @RequestHeader(value = "Authorization", required = false) String authorization) {
        return campaignService.findRules(campaignID, pageable, authorization);
    }
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag(s) were found.", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TagDTO.TagResponse.class)))),
            @ApiResponse(responseCode = "404", description = "Campaign not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Campaign not found by ID example.", value = "{\"status\": 404, \"message\": \"Campaign was not found with value: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example.", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Get Rules by Tags from a Campaign ID.", description = "Returns the tags with rules's public data. Need the \"Authorization\" token if private campaigns are asked. Returns 404 if no campaign with this ID exists.")
    @GetMapping("/rules/tags/{campaignID}")
    public List<TagDTO.TagResponse> GetRulesPerTags(@PathVariable Integer campaignID, @Nullable Pageable pageable, @RequestHeader(value = "Authorization", required = false) String authorization) {
        return campaignService.findRulesPerTags(campaignID, pageable, authorization);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Campaign was created.", content = @Content(schema = @Schema(implementation = CampaignDTO.CampaignResponse.class))),
            @ApiResponse(responseCode = "409", description = "Illegal argument.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Unknown Rule ID.", value = "{\"status\": 409, \"message\": \"One or more rule IDs do not exist.\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example.", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Create a campaign.", description = "Returns the campaigns's public data. The campaign users can be added by updating.")
    @PostMapping
    public CampaignDTO.CampaignResponse createCampaign(@Valid @RequestBody CampaignDTO.CampaignCreateRequest campaign, @RequestHeader("Authorization") String authorization) {
        return campaignService.createCampaign(campaign, authorization);
    }
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Campaign was updated.", content = @Content(schema = @Schema(implementation = CampaignDTO.CampaignResponse.class))),
            @ApiResponse(responseCode = "404", description = "Campaign not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Campaign not found by ID example.", value = "{\"status\": 404, \"message\": \"Campaign was not found with value: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "409", description = "Illegal argument.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Unknown Rule ID.", value = "{\"status\": 409, \"message\": \"One or more rule IDs do not exist.\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example.", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Update a campaign.", description = "Returns the campaigns's public data.")
    @PostMapping("/edit")
    public CampaignDTO.CampaignResponse editCampaign(@Valid @RequestBody CampaignDTO.CampaignEditRequest campaign, @RequestHeader("Authorization") String authorization) {
        return campaignService.editCampaign(campaign, authorization);
    }

    //Careful no check if User can join or not because of private campaign.
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User was added to campaign.", content = @Content(schema = @Schema(implementation = CampaignDTO.CampaignResponse.class))),
            @ApiResponse(responseCode = "404", description = "Campaign not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Campaign not found by ID example.", value = "{\"status\": 404, \"message\": \"Campaign was not found with value: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "409", description = "Illegal argument.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "User already part of campaign.", value = "{\"status\": 409, \"message\": \"Already part of campaign.\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example.", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Join a campaign.", description = "Returns the campaigns's public data.")
    @PostMapping("/join/{campaignID}")
    public CampaignDTO.CampaignResponse joinCampaign(@PathVariable Integer campaignID, @RequestHeader("Authorization") String authorization) {
        return campaignService.joinCampaign(campaignID, authorization);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active campaign was set..", content = @Content(schema = @Schema(implementation = CampaignDTO.CampaignResponse.class))),
            @ApiResponse(responseCode = "404", description = "Campaign not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "No campaign set as active example.", value = "{\"status\": 404, \"message\": \"Campaign was not found with value: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example.", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Set the active campaign of a user.", description = "Returns the campaigns's public data.")
    @GetMapping("/active")
    public CampaignDTO.CampaignResponse getActiveCampaign(@RequestHeader("Authorization") String authorization) {
        return campaignService.getActiveCampaign(authorization);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Active campaign was found.", content = @Content(schema = @Schema(implementation = CampaignDTO.CampaignResponse.class))),
            @ApiResponse(responseCode = "404", description = "Active campaign not found.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "No campaign set as active example.", value = "{\"status\": 404, \"message\": \"Campaign was not found with value: 26\", \"timestamp\": \"2026-01-15T10:30:00\"}")})),
            @ApiResponse(responseCode = "401", description = "Wrong Credentials.", content = @Content(schema = @Schema(implementation = ErrorResponse.class), examples = {@ExampleObject(name = "Wrong Credentials example.", value = "{\"status\": 401, \"message\": \"Wrong Credentials.\", \"timestamp\": \"2026-01-15T10:30:00\"}")}))
    })
    @SecurityRequirement(name = "Authorization")
    @Operation(summary = "Set the active campaign of a user.", description = "Returns the campaigns's public data.")
    @PostMapping("/active/{campaignID}")
    public CampaignDTO.CampaignResponse setActiveCampaign(@PathVariable Integer campaignID, @RequestHeader("Authorization") String authorization) {
        return campaignService.setActiveCampaign(campaignID, authorization);
    }
}
