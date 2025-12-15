package at.learnhub.boundary;

import at.learnhub.dto.request.QuestionCreationRequestDto;
import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.dto.simple.QuestionUpdateRequestDto;
import at.learnhub.mapper.QuestionMapper;
import at.learnhub.model.Question;
import at.learnhub.model.QuestionType;
import at.learnhub.repository.QuestionRepository;
import at.learnhub.security.CustomSecurityContext;
import at.learnhub.service.QuestionService;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.Map;

@Path("/api/questions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "bearer")
public class QuestionResource {

    @Inject
    QuestionRepository questionRepository;

    @Inject
    QuestionService questionService;

    /**
     * Get all questions - requires authentication
     */
    @GET
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
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Response getAllQuestions(@Context SecurityContext securityContext) {
        System.out.println("\n[QuestionResource] Getting all questions");

        if (securityContext instanceof CustomSecurityContext customContext) {
            System.out.println("[QuestionResource] User: " + customContext.getUsername());
        }

        try {
            List<QuestionDto> questions = questionRepository.findAll();
            System.out.println("[QuestionResource] Found " + questions.size() + " questions");
            return Response.ok(questions).build();
        } catch (Exception e) {
            System.err.println("[QuestionResource] Error: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error loading questions", "message", e.getMessage()))
                    .build();
        }
    }

    /**
     * Get question by ID
     */
    @GET
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
                    responseCode = "401",
                    description = "Unauthorized - authentication required"
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
            @PathParam("id") Long id,
            @Context SecurityContext securityContext
    ) {
        System.out.println("\n[QuestionResource] Getting question ID: " + id);

        try {
            QuestionDto question = questionRepository.getQuestionDtoById(id);
            return Response.ok(question).build();
        } catch (Exception e) {
            System.err.println("[QuestionResource] Question not found: " + id);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Question not found", "id", id))
                    .build();
        }
    }

    /**
     * Get questions by topic pool
     */
    @GET
    @Path("/byTopicPool/{topicPoolId}")
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
                    responseCode = "401",
                    description = "Unauthorized - authentication required"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Topic Pool not found"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Response getQuestionsByTopicPool(
            @Parameter(
                    description = "ID of the topic pool which holds questions",
                    required = true,
                    example = "123"
            )
            @PathParam("topicPoolId") Long topicPoolId,
            @Context SecurityContext securityContext
    ) {
        System.out.println("\n[QuestionResource] Getting questions for topic pool: " + topicPoolId);

        if (securityContext instanceof CustomSecurityContext customContext) {
            System.out.println("[QuestionResource] User: " + customContext.getUsername());
        }

        try {
            List<QuestionDto> questions = questionService.getQuestionsByTopicPool(topicPoolId);
            System.out.println("[QuestionResource] Found " + questions.size() + " questions");
            return Response.ok(questions).build();
        } catch (Exception e) {
            System.err.println("[QuestionResource] Error: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error loading questions", "message", e.getMessage()))
                    .build();
        }
    }

    /**
     * Get questions by user ID
     */
    @GET
    @Path("/user/{userId}")
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
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Response getQuestionsByUserId(
            @Parameter(
                    description = "id of the user whose questions are searched",
                    required = true,
                    example = "1"
            )
            @PathParam("userId") Long userId,
            @Context SecurityContext securityContext
    ) {
        System.out.println("\n[QuestionResource] Getting questions for user: " + userId);

        try {
            List<QuestionDto> questions = questionRepository.findByUserId(userId);
            return Response.ok(questions).build();
        } catch (Exception e) {
            System.err.println("[QuestionResource] Error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error loading questions", "message", e.getMessage()))
                    .build();
        }
    }

    /**
     * Get public questions
     */
    @GET
    @Path("/public")
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
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Response getPublicQuestions(@Context SecurityContext securityContext) {
        System.out.println("\n[QuestionResource] Getting public questions");

        try {
            List<QuestionDto> questions = questionRepository.findAllPublicQuestions();
            return Response.ok(questions).build();
        } catch (Exception e) {
            System.err.println("[QuestionResource] Error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error loading questions", "message", e.getMessage()))
                    .build();
        }
    }

    /**
     * Get questions by type
     */
    @GET
    @Path("/type/{type}")
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
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required"
            )
    })
    public Response getQuestionsByType(
            @Parameter(
                    description = "type of the question",
                    required = true,
                    example = "FREETEXT"
            )
            @PathParam("type") String type,
            @Context SecurityContext securityContext
    ) {
        System.out.println("\n[QuestionResource] Getting questions by type: " + type);

        try {
            QuestionType questionType = QuestionType.valueOf(type.toUpperCase());
            List<QuestionDto> questions = questionRepository.findByType(questionType);
            return Response.ok(questions).build();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage());
        }
    }

    /**
     * Get questions by difficulty
     */
    @GET
    @Path("/difficulty/{level}")
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
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required"
            )
    })
    public Response getQuestionsByDifficulty(
            @Parameter(
                    description = "Difficulty level (1=easy,2=medium,3=hard)",
                    required = true,
                    example = "2"
            )
            @PathParam("level") Integer level,
            @Context SecurityContext securityContext
    ) {
        System.out.println("\n[QuestionResource] Getting questions by difficulty: " + level);

        List<QuestionDto> questions = questionRepository.findByDifficulty(level);
        return Response.ok(questions).build();
    }

    /**
     * Get only question IDs
     */
    @GET
    @Path("/ids")
    @Operation(
            summary = "Get only question IDs",
            description = "Returns only the IDs of questions, either all or filtered by topic pool ID"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "List of question IDs",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = Long.class
                            )
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Missing required parameters"
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Response getQuestionIds(
            @Parameter(
                    description = "Optional topic pool id to filter questions",
                    required = false,
                    example = "5"
            )
            @QueryParam("topicPoolId") Long topicPoolId,
            @Parameter(
                    description = "User id to filter questions",
                    required = true,
                    example = "1"
            )
            @QueryParam("userId") Long userId,
            @Context SecurityContext securityContext
    ) {
        System.out.println("\n[QuestionResource] Getting question IDs");
        System.out.println("[QuestionResource] userId: " + userId + ", topicPoolId: " + topicPoolId);

        if (userId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "User ID is required"))
                    .build();
        }

        try {
            List<Long> ids = questionService.getQuestionIds(topicPoolId, userId);
            System.out.println("[QuestionResource] Found " + ids.size() + " question IDs");
            return Response.ok(ids).build();
        } catch (Exception e) {
            System.err.println("[QuestionResource] Error getting question IDs: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error getting question IDs", "message", e.getMessage()))
                    .build();
        }
    }

    /**
     * Create a new question
     */
    @POST
    @Transactional
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
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Response createQuestion(
            @RequestBody(
                    required = true,
                    description = "The full question DTO to create",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = QuestionCreationRequestDto.class)
                    )
            )
            QuestionCreationRequestDto dto,
            @Context SecurityContext securityContext
    ) {
        System.out.println("\n[QuestionResource] Creating new question");
        System.out.println("[QuestionResource] DTO: " + dto);

        if (securityContext instanceof CustomSecurityContext customContext) {
            String keycloakSub = customContext.getKeycloakSub();
            System.out.println("[QuestionResource] Creator: " + customContext.getUsername());
            System.out.println("[QuestionResource] Keycloak Sub: " + keycloakSub);
        }

        try {
            if (dto.text() == null || dto.text().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Question text is required"))
                        .build();
            }

            if (dto.userId() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "User ID is required"))
                        .build();
            }

            if (dto.topicPoolId() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Topic pool ID is required"))
                        .build();
            }

            QuestionDto resultDto = questionService.create(dto);

            System.out.println("[QuestionResource] ✓ Question created with ID: " + (resultDto != null ? resultDto.id() : "unknown"));

            return Response.status(Response.Status.CREATED)
                    .entity(resultDto)
                    .build();

        } catch (Exception e) {
            System.err.println("[QuestionResource] ✗ Error creating question: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error creating question", "message", e.getMessage()))
                    .build();
        }
    }

    /**
     * Update a question (PATCH)
     */
    @PATCH
    @Path("/{id}")
    @Transactional
    @Operation(
            summary = "Update an existing question",
            description = "Updates only provided fields of a question"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Question updated",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = QuestionDto.class)
                    )
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Question not found"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Response updateQuestion(
            @Parameter(
                    description = "ID of the question to update",
                    required = true,
                    example = "123"
            )
            @PathParam("id") Long id,
            @RequestBody(
                    description = "Question update data",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = QuestionUpdateRequestDto.class)
                    )
            )
            QuestionUpdateRequestDto dto,
            @Context SecurityContext securityContext
    ) {
        System.out.println("\n[QuestionResource] Updating question: " + id);

        if (securityContext instanceof CustomSecurityContext customContext) {
            System.out.println("[QuestionResource] User: " + customContext.getUsername());
        }

        try {
            Question question = questionRepository.updateQuestion(id, dto);

            System.out.println("[QuestionResource] ✓ Question updated: " + id);

            QuestionDto resultDto = QuestionMapper.toDto(question);

            return Response.ok(resultDto).build();

        } catch (Exception e) {
            System.err.println("[QuestionResource] Error updating: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error updating question", "message", e.getMessage()))
                    .build();
        }
    }

    /**
     * Delete a question
     */
    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(
            summary = "Delete a question by ID",
            description = "Deletes a question including all associated answers (cascade delete in DB)"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "204",
                    description = "Question deleted successfully"
            ),
            @APIResponse(
                    responseCode = "401",
                    description = "Unauthorized - authentication required"
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Question not found"
            ),
            @APIResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public Response deleteQuestion(
            @Parameter(
                    description = "ID of the question to delete",
                    required = true,
                    example = "123"
            )
            @PathParam("id") Long id,
            @Context SecurityContext securityContext
    ) {
        System.out.println("\n[QuestionResource] Deleting question: " + id);

        if (securityContext instanceof CustomSecurityContext customContext) {
            System.out.println("[QuestionResource] User: " + customContext.getUsername());
        }

        try {
            questionRepository.deleteQuestion(id);
            System.out.println("[QuestionResource] ✓ Question deleted: " + id);
            return Response.noContent().build();

        } catch (Exception e) {
            System.err.println("[QuestionResource] Error deleting: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error deleting question", "message", e.getMessage()))
                    .build();
        }
    }
}