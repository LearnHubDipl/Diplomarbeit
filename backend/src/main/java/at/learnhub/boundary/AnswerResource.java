package at.learnhub.boundary;

import at.learnhub.dto.response.CheckAnswersResponseDto;
import at.learnhub.dto.simple.AnswerDto;
import at.learnhub.dto.request.CheckAnswersRequestDto;
import at.learnhub.repository.AnswerRepository;
import at.learnhub.service.AnswerService;
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

import java.util.List;

@Path("/api/answers")
public class AnswerResource {
    @Inject
    AnswerRepository answerRepository;
    @Inject
    AnswerService answerService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "List all answers",
            description = "Returns a list of all answers as DTOs"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "List of answers",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = AnswerDto.class
                            )
                    )
            )
    })
    public Response getAnswerList() {
        List<AnswerDto> answers = answerRepository.findAll();
        return Response.ok(answers).build();
    }

    @POST
    @Path("/check")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Check if the selected answers for a question are correct",
            description = "Returns whether the submitted answers are correct and what the correct answers are"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Answer checked successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = CheckAnswersResponseDto.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid request or missing data"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Question not found"
            )
    })
    public Response checkAnswers(
            @RequestBody
            @Parameter(
                    description = "Submitted answers with question ID",
                    required = true,
                    schema = @Schema(implementation = CheckAnswersRequestDto.class)
            )
            CheckAnswersRequestDto request
    ) {
        CheckAnswersResponseDto result = answerService.checkAnswers(request);
        return Response.ok(result).build();
    }
}

