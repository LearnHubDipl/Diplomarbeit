package at.learnhub.boundary;

import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.repository.QuestionRepository;
import at.learnhub.service.QuestionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.util.List;

@Path("/api/questions")
public class QuestionResource {
    @Inject
    QuestionRepository questionRepository;
    @Inject
    QuestionService questionService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "List all questions",
            description = "Returns a list of all questions as DTOs"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "List of questions",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = QuestionDto.class
                            )
                    )
            )
    })
    public Response getQuestionList() {
        List<QuestionDto> questions = questionRepository.findAll();
        return Response.ok(questions).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @Operation(
            summary = "Get a single question by ID",
            description = "Returns a single Question DTO for the given ID"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Question found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = QuestionDto.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Question not found"
            )
    })
    public Response getQuestionById(
            @Parameter(
                    description = "ID of the question to fetch",
                    required = true,
                    example = "123"
            )
            @PathParam("id") Long id) {
        QuestionDto question = questionRepository.getQuestionDtoById(id);
        return Response.ok(question).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/byTopicPool/{id}")
    @Operation(
            summary = "Get a list of questions for a topic pool by the topic pool ID",
            description = "Returns a list of Questions as their DTO that are associated with the topic pool of the given ID"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Topic Pool found and all Questions associated returned",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = QuestionDto.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Topic Pool not found"
            )
    })
    public Response getQuestionsForTopicPool(
            @Parameter(
                    description = "ID of the topic pool which holds questions",
                    required = true,
                    example = "123"
            )
            @PathParam("id") Long id) {
        List<QuestionDto> questions = questionService.getQuestionsByTopicPool(id);
        return Response.ok(questions).build();
    }
}

