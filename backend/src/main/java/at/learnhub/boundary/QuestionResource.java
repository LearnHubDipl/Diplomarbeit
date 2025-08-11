package at.learnhub.boundary;

import at.learnhub.dto.request.QuestionCreationRequestDto;
import at.learnhub.model.QuestionType;
import at.learnhub.repository.QuestionRepository;
import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.service.QuestionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
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

    @GET
    @Path("/user/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get questions from a specific user by their user id",
            description = "Returns a list of all questions created by a user"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "List of questions by a user",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    implementation = QuestionDto.class,
                                    type = SchemaType.ARRAY
                            )
                    )
            )
    })
    public Response getQuestionByUserId(
            @Parameter(description = "id of the user whose questions are searched", required = true)
            @PathParam("userId") Long userId) {
        List<QuestionDto> questions = questionRepository.findByUserId(userId);
        return Response.ok(questions).build();
    }

    @GET
    @Path("/public")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get all questions which are public",
            description = "Returns a list of all questions that are public"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "List of public questions",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    implementation = QuestionDto.class,
                                    type = SchemaType.ARRAY
                            )
                    )
            )
    })
    public Response getPublicQuestions() {
        List<QuestionDto> questions = questionRepository.findAllPublicQuestions();
        return Response.ok(questions).build();
    }

    @GET
    @Path("/type/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get questions by type",
            description = "Returns all questions of a specific type (f.e. freetext, multiple choice)"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "List of questions matching the searched type",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    implementation = QuestionDto.class,
                                    type = SchemaType.ARRAY
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid question type"
            )
    })
    public Response getQuestionsByType(
            @Parameter(description = "type of the question", required = true, example = "FREETEXT")
            @PathParam("type") String type
    ) {
        try {
            QuestionType questionType = QuestionType.valueOf(type.toUpperCase());
            List<QuestionDto> questions = questionRepository.findByType(questionType);
            return Response.ok(questions).build();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    @GET
    @Path("/difficulty/{level}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get questions by difficulty",
            description = "Returns all questions with the specific difficulty (f.e. 1=easy, 3=hard)"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "List of questions with matching difficulty",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    implementation = QuestionDto.class,
                                    type = SchemaType.ARRAY
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid difficulty level"
            )
    })
    public Response getQuestionsByDifficulty(
            @Parameter(description = "Difficulty level (1=easy,2=medium,3=hard)", required = true, example = "2")
            @PathParam("level") Integer level
    ) {
        List<QuestionDto> questions = questionRepository.findByDifficulty(level);
        return Response.ok(questions).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create a new question",
            description = "Creates a new question of any type and returns the created object"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Question created successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = QuestionDto.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid input data"
            )
    })
    public Response createQuestion(
            @RequestBody(
                    required = true,
                    description = "The full questionDto to create",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = QuestionCreationRequestDto.class)
                    )
            )
            QuestionCreationRequestDto request
    ) {
        if (request == null || request.text() == null || request.type() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Missing required fields").build();
        }
        QuestionDto created = questionService.create(request);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
}
