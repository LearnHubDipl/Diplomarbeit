package at.learnhub.boundary;

import at.learnhub.dto.request.AddQuestionToQuestionPoolRequestDto;
import at.learnhub.dto.request.CheckAnswersRequestDto;
import at.learnhub.dto.response.CheckAnswersResponseDto;
import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.dto.simple.QuestionPoolDto;
import at.learnhub.model.QuestionPool;
import at.learnhub.model.StreakTracking;
import at.learnhub.model.User;
import at.learnhub.repository.QuestionPoolRepository;
import at.learnhub.repository.StreakTrackingRepository;
import at.learnhub.service.QuestionPoolService;
import at.learnhub.service.StreakTrackingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path("/api/questionPools")
@Produces(MediaType.APPLICATION_JSON)
public class QuestionPoolResource {
    @Inject
    QuestionPoolRepository questionPoolRepository;
    @Inject
    QuestionPoolService questionPoolService;

    @GET
    @Path("/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get question pool of a specific user by their user ID",
            description = "Returns a question pool with all its entries"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "The question pool",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    implementation = QuestionPoolDto.class
                            )
                    )
            )
    })
    public Response getQuestionPool(@Parameter(
            description = "ID of the user owning the question pool to be fetched",
            required = true,
            example = "1"
    ) @PathParam("userId") Long userId) {
        QuestionPoolDto questionPool = questionPoolRepository.findByUserId(userId);
        return Response.ok(questionPool).build();
    }


    @POST
    @Path("/addQuestions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Add Given Questions to the Question Pool determined by the given user id",
            description = "Returns the updated question pool with its new entries"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Questions added successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = QuestionPoolDto.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Requested Question pool or questions don't exist!"
            )
    })
    public Response addQuestions(
            @RequestBody
            @Parameter(
                    description = "Questions to be added to the question pool and its user id",
                    required = true,
                    schema = @Schema(implementation = AddQuestionToQuestionPoolRequestDto.class)
            )
            AddQuestionToQuestionPoolRequestDto request
    ) {
        QuestionPoolDto pool = questionPoolService.addQuestionsToPool(request);
        return Response.ok(pool).build();
    }

}
