package at.learnhub.boundary;

import at.learnhub.dto.request.CheckAnswersRequestDto;
import at.learnhub.dto.response.CheckAnswersResponseDto;
import at.learnhub.dto.simple.UserSlimDto;
import at.learnhub.repository.AnswerRepository;
import at.learnhub.repository.UserRepository;
import at.learnhub.service.AnswerService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path("/api/users")
public class UserResource {
    @Inject
    UserRepository userRepository;

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get a user by ID",
            description = "Get a user based on the provided ID"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "User found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = UserSlimDto.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "User not found"
            )
    })
    public Response checkAnswers(
            @Parameter(
                    description = "ID of the user to be fetched",
                    required = true
            )
            @PathParam("id") Long id
    ) {
        UserSlimDto user = userRepository.getUserSlimDtoById(id);
        return Response.ok(user).build();
    }
}

