package at.learnhub.boundary;

import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.dto.simple.SubjectDto;
import at.learnhub.model.QuestionType;
import at.learnhub.repository.QuestionRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;


import java.util.List;

/**
 * Rest Api resource for retrieving question data.
 */
@Path("/api/questions")
public class QuestionResource {
    @Inject
    QuestionRepository questionRepository;

    /**
     * Returns a list of all questions in the system.
     * @return HTTP 200 Ok with list of QuestionDto
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get all question",
            description = "Returns a list of all questions as DTOs"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "List of questions",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    implementation = QuestionDto.class,
                                    type = SchemaType.ARRAY
                            )
                    )
            )
    })
    public Response getAllQuestions() {
        List<QuestionDto> questions = questionRepository.findAll();
        return Response.ok(questions).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get question by id",
            description = "Returns a question from the searched id"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Question with searched id",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    implementation = QuestionDto.class,
                                    type = SchemaType.ARRAY
                            )
                    )
            )
    })
    public Response getQuestionById(@Parameter(description = "id of the question searched", required = true) @PathParam("id") Long questionId) {
        QuestionDto question = questionRepository.findById(questionId);
        if (question == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(question).build();
    }

    @GET
    @Path("/user/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get question from a specific user by their user id",
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
    public Response getQuestionByUserId(@Parameter(description = "id of the user whose questions are searched", required = true) @PathParam("userId") Long userId) {
        List<QuestionDto> questions = questionRepository.findByUserId(userId);
        if (questions == null || questions.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
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
    public Response getPublicQuestions(){
        List<QuestionDto> questions = questionRepository.findAllPublicQuestions();
        if(questions == null || questions.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
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
                    responseCode = "204",
                    description = "No questions of this type found"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid question type"
            )
    })
    public Response getQuestionsByType(@Parameter(description = "type of the question", required = true) @PathParam("type") String type){
        try{
            QuestionType questionType = QuestionType.valueOf(type.toUpperCase());
            List<QuestionDto> questions = questionRepository.findByType(questionType);

            if(questions == null || questions.isEmpty()) {
                return Response.status(Response.Status.NO_CONTENT).build();
            }
            return Response.ok(questions).build();
        } catch(IllegalArgumentException e){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid question type: " + type).build();
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
                    responseCode = "204",
                    description = "No questions with this difficulty found"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid difficulty level"
            )
    })
    public Response getQuestionsByDifficulty(
            @Parameter(description = "Difficulty level (1=easy,2=medium,3=hard)",required = true,example = "2")
            @PathParam("level") Integer level
    ) {
        if(level < 1 || level > 3){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid difficulty level: " + level).build();
        }

        List<QuestionDto> questions = questionRepository.findByDifficulty(level);
        if(questions == null || questions.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.ok(questions).build();
    }

    @GET
    @Path("/topic-pool/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Get questions by topic pool",
            description = "Returns all questions with the searched topic pool"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "List of questions with matching topic pool",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    implementation = QuestionDto.class,
                                    type = SchemaType.ARRAY
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "204",
                    description = "No questions with this topic pool"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid topic pool"
            )
    })
    public Response getQuestionsByTopicPoolId(
            @Parameter(description = "id of the topic pool", required = true, example = "3")
            @PathParam("id") Long topicPoolId
    ){
        List<QuestionDto> questions = questionRepository.findByTopicPoolId(topicPoolId);
        if(questions == null || questions.isEmpty()) {
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.ok(questions).build();
    }

}
