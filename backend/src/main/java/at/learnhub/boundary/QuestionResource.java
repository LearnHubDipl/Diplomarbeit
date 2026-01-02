package at.learnhub.boundary;

import at.learnhub.dto.request.QuestionCreationRequestDto;
import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.dto.simple.QuestionUpdateRequestDto;
import at.learnhub.mapper.QuestionMapper;
import at.learnhub.model.Question;
import at.learnhub.model.QuestionType;
import at.learnhub.model.User;
import at.learnhub.repository.QuestionRepository;
import at.learnhub.repository.UserRepository;
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/api/questions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SecurityRequirement(name = "bearer")
public class QuestionResource {

    @Inject
    QuestionRepository questionRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    QuestionService questionService;

    /**
     * Helper method to extract CustomSecurityContext from proxy
     */
    private CustomSecurityContext extractCustomSecurityContext(SecurityContext securityContext) {
        if (securityContext == null) {
            return null;
        }

        if (securityContext instanceof CustomSecurityContext) {
            return (CustomSecurityContext) securityContext;
        }

        if (Proxy.isProxyClass(securityContext.getClass())) {
            try {
                InvocationHandler handler = Proxy.getInvocationHandler(securityContext);
                if (handler instanceof jakarta.ws.rs.core.SecurityContext) {
                    return extractCustomSecurityContext((SecurityContext) handler);
                }
            } catch (Exception e) {
                System.err.println("[QuestionResource] Error extracting from proxy: " + e.getMessage());
            }
        }

        return null;
    }

    /**
     * Get current user from security context
     */
    private User getCurrentUser(SecurityContext securityContext) {
        if (securityContext == null) {
            System.err.println("[getCurrentUser] No security context");
            throw new NotAuthorizedException("No security context");
        }

        CustomSecurityContext customContext = extractCustomSecurityContext(securityContext);
        String keycloakSub = null;

        if (customContext != null) {
            keycloakSub = customContext.getKeycloakSub();
            //System.out.println("[getCurrentUser] Got keycloakSub from CustomContext: " + keycloakSub);
        } else {
            Principal principal = securityContext.getUserPrincipal();
            if (principal != null) {
                keycloakSub = principal.getName();
                //System.out.println("[getCurrentUser] Got keycloakSub from Principal: " + keycloakSub);
            }
        }

        if (keycloakSub == null) {
            System.err.println("[getCurrentUser] No user principal found");
            throw new NotAuthorizedException("No user principal found");
        }

        //System.out.println("[getCurrentUser] Searching for user with keycloakSub: " + keycloakSub);
        Optional<User> userOptional = userRepository.findUserEntityByKeycloakSub(keycloakSub);

        if (userOptional.isEmpty()) {
            System.err.println("[getCurrentUser] User not found in database for keycloakSub: " + keycloakSub);
            System.err.println("[getCurrentUser] Did you call /api/users/register first?");
            throw new NotFoundException("User not found in database");
        }

        User user = userOptional.get();
        //System.out.println("[getCurrentUser] User found: " + user.getName() + " (ID: " + user.getId() + ", isAdmin: " + user.getAdmin() + ")");

        return user;
    }

    /**
     * Check if user can modify a question
     */
    private boolean canModifyQuestion(User currentUser, Question question) {
        if (Boolean.TRUE.equals(currentUser.getAdmin()) && Boolean.TRUE.equals(question.getPublic())) {
            return true;
        }

        return question.getUser().getId().equals(currentUser.getId());
    }

    @GET
    @Operation(summary = "List all questions", description = "Returns a list of all questions as DTOs")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "List of questions",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = SchemaType.ARRAY, implementation = QuestionDto.class))),
            @APIResponse(responseCode = "401", description = "Unauthorized"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAllQuestions(@Context SecurityContext securityContext) {
        //System.out.println("\n[QuestionResource] Getting all questions");

        if (securityContext instanceof CustomSecurityContext customContext) {
            //System.out.println("[QuestionResource] User: " + customContext.getUsername());
        }

        try {
            List<QuestionDto> questions = questionRepository.findAll();
            //System.out.println("[QuestionResource] Found " + questions.size() + " questions");
            return Response.ok(questions).build();
        } catch (Exception e) {
            System.err.println("[QuestionResource] Error: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error loading questions", "message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get a single question by ID")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Question found"),
            @APIResponse(responseCode = "401", description = "Unauthorized"),
            @APIResponse(responseCode = "404", description = "Question not found")
    })
    public Response getQuestionById(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        //System.out.println("\n[QuestionResource] Getting question ID: " + id);

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

    @GET
    @Path("/byTopicPool/{topicPoolId}")
    @Operation(summary = "Get questions for a topic pool")
    public Response getQuestionsByTopicPool(
            @PathParam("topicPoolId") Long topicPoolId,
            @Context SecurityContext securityContext) {
        //System.out.println("\n[QuestionResource] Getting questions for topic pool: " + topicPoolId);

        try {
            List<QuestionDto> questions = questionService.getQuestionsByTopicPool(topicPoolId);
            //System.out.println("[QuestionResource] Found " + questions.size() + " questions");
            return Response.ok(questions).build();
        } catch (Exception e) {
            System.err.println("[QuestionResource] Error: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error loading questions", "message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/user/{userId}")
    @Operation(summary = "Get questions from a specific user")
    public Response getQuestionsByUserId(@PathParam("userId") Long userId, @Context SecurityContext securityContext) {
        //System.out.println("\n[QuestionResource] Getting questions for user: " + userId);

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

    @GET
    @Path("/public")
    @Operation(summary = "Get all public questions")
    public Response getPublicQuestions(@Context SecurityContext securityContext) {
        //System.out.println("\n[QuestionResource] Getting public questions");

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

    @GET
    @Path("/private")
    @Operation(summary = "Get all private questions (admin only)",
            description = "Returns all questions where isPublic = false. Only accessible by admins.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "List of private questions",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = SchemaType.ARRAY, implementation = QuestionDto.class))),
            @APIResponse(responseCode = "403", description = "Forbidden - Only admins can access"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response getAllPrivateQuestions(@Context SecurityContext securityContext) {
        try {
            User currentUser = getCurrentUser(securityContext);

            if (!Boolean.TRUE.equals(currentUser.getAdmin())) {
                //System.out.println("[QuestionResource] Non-admin tried to access private questions");
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "Only admins can view all private questions"))
                        .build();
            }

            List<QuestionDto> questions = questionRepository.findAllPrivateQuestions();
            return Response.ok(questions).build();

        } catch (NotAuthorizedException | NotFoundException e) {
            System.err.println("[QuestionResource] Auth error: " + e.getMessage());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            System.err.println("[QuestionResource] Error: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error loading private questions", "message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/awaiting-approval")
    @Operation(summary = "Get all private questions (admin only)",
            description = "Returns all questions where isPublic = false. Only accessible by admins.")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "List of private questions",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(type = SchemaType.ARRAY, implementation = QuestionDto.class))),
            @APIResponse(responseCode = "403", description = "Forbidden - Only admins can access"),
            @APIResponse(responseCode = "500", description = "Internal server error")
    })
    public Response findAllQuestionsWithApprovalRequested(@Context SecurityContext securityContext) {
        try {
            User currentUser = getCurrentUser(securityContext);

            if (!Boolean.TRUE.equals(currentUser.getAdmin())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "Only admins can view all private questions"))
                        .build();
            }

            List<QuestionDto> questions = questionRepository.findAllQuestionsWithApprovalRequested();
            return Response.ok(questions).build();

        } catch (NotAuthorizedException | NotFoundException e) {
            System.err.println("[QuestionResource] Auth error: " + e.getMessage());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            System.err.println("[QuestionResource] Error: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error loading private questions", "message", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/type/{type}")
    @Operation(summary = "Get questions by type")
    public Response getQuestionsByType(@PathParam("type") String type, @Context SecurityContext securityContext) {
        //System.out.println("\n[QuestionResource] Getting questions by type: " + type);

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
    @Operation(summary = "Get questions by difficulty")
    public Response getQuestionsByDifficulty(@PathParam("level") Integer level, @Context SecurityContext securityContext) {
        //System.out.println("\n[QuestionResource] Getting questions by difficulty: " + level);

        List<QuestionDto> questions = questionRepository.findByDifficulty(level);
        return Response.ok(questions).build();
    }

    @GET
    @Path("/ids")
    @Operation(summary = "Get only question IDs")
    public Response getQuestionIds(
            @QueryParam("topicPoolId") Long topicPoolId,
            @QueryParam("userId") Long userId,
            @Context SecurityContext securityContext) {
        // System.out.println("\n[QuestionResource] Getting question IDs");
        // System.out.println("[QuestionResource] userId: " + userId + ", topicPoolId: " + topicPoolId);

        if (userId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "User ID is required"))
                    .build();
        }

        try {
            List<Long> ids = questionService.getQuestionIds(topicPoolId, userId);
            // System.out.println("[QuestionResource] Found " + ids.size() + " question IDs");
            return Response.ok(ids).build();
        } catch (Exception e) {
            System.err.println("[QuestionResource] Error getting question IDs: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error getting question IDs", "message", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Transactional
    @Operation(summary = "Create a new question")
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Question created"),
            @APIResponse(responseCode = "400", description = "Invalid input"),
            @APIResponse(responseCode = "403", description = "Forbidden - Not allowed to create public questions")
    })
    public Response createQuestion(
            @RequestBody(required = true, description = "The full question DTO to create")
            QuestionCreationRequestDto dto,
            @Context SecurityContext securityContext) {
        // System.out.println("\n[QuestionResource] Creating new question");
        // System.out.println("[QuestionResource] DTO: " + dto);

        try {
            User currentUser = getCurrentUser(securityContext);
            // System.out.println("[QuestionResource] Creator: " + currentUser.getName() + " (ID: " + currentUser.getId() + ")");

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

            if (Boolean.TRUE.equals(dto.isPublic()) && !Boolean.TRUE.equals(currentUser.getAdmin())) {
                // System.out.println("[QuestionResource] Non-admin tried to create public question");
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "Only admins can create public questions"))
                        .build();
            }

            // Security check: User can only create questions for themselves
            if (!dto.userId().equals(currentUser.getId())) {
                System.out.println("[QuestionResource] User tried to create question for another user");
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "You can only create questions for yourself"))
                        .build();
            }

            QuestionDto resultDto = questionService.create(dto);

            // System.out.println("[QuestionResource] Question created with ID: " + resultDto.id());
            // System.out.println("[QuestionResource] Is public: " + resultDto.isPublic());

            return Response.status(Response.Status.CREATED)
                    .entity(resultDto)
                    .build();

        } catch (NotAuthorizedException | NotFoundException e) {
            System.err.println("[QuestionResource] Auth error: " + e.getMessage());
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            System.err.println("[QuestionResource] Error creating question: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error creating question", "message", e.getMessage()))
                    .build();
        }
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Update an existing question")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Question updated"),
            @APIResponse(responseCode = "403", description = "Forbidden - Cannot modify this question"),
            @APIResponse(responseCode = "404", description = "Question not found")
    })
    public Response updateQuestion(
            @PathParam("id") Long id,
            @RequestBody(description = "Question update data")
            QuestionUpdateRequestDto dto,
            @Context SecurityContext securityContext) {
        System.out.println("\n[QuestionResource] Updating question: " + id);

        try {
            User currentUser = getCurrentUser(securityContext);

            Question question = questionRepository.findById(id);
            if (question == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Question not found"))
                        .build();
            }

            if (!canModifyQuestion(currentUser, question)) {
                System.out.println("[QuestionResource] User not allowed to modify question " + id);
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "You can only modify your own questions or public questions as admin"))
                        .build();
            }

            if (dto.isPublic() != null && dto.isPublic() && !Boolean.TRUE.equals(currentUser.getAdmin())) {
                // System.out.println("[QuestionResource] Non-admin tried to make question public");
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "Only admins can make questions public"))
                        .build();
            }

            Question updatedQuestion = questionRepository.updateQuestion(id, dto);
            QuestionDto resultDto = QuestionMapper.toDto(updatedQuestion);

            // System.out.println("[QuestionResource] Question updated: " + id);

            return Response.ok(resultDto).build();

        } catch (NotAuthorizedException | NotFoundException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            System.err.println("[QuestionResource] Error updating: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error updating question", "message", e.getMessage()))
                    .build();
        }
    }

    @PATCH
    @Path("/{id}/public")
    @Transactional
    @Operation(summary = "Change public status of a question (admin only)")
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Public status changed"),
            @APIResponse(responseCode = "403", description = "Forbidden - Only admins can change public status"),
            @APIResponse(responseCode = "404", description = "Question not found")
    })
    public Response changeQuestionPublicStatus(
            @PathParam("id") Long id,
            @QueryParam("isPublic") @Parameter(description = "New public status (true/false)", required = true) Boolean isPublic,
            @Context SecurityContext securityContext) {
        System.out.println("\n[QuestionResource] Changing public status for question: " + id + " to: " + isPublic);

        try {
            User currentUser = getCurrentUser(securityContext);

            if (!Boolean.TRUE.equals(currentUser.getAdmin())) {
                //System.out.println("[QuestionResource] Non-admin tried to change public status");
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "Only admins can change public status of questions"))
                        .build();
            }

            Question question = questionRepository.findById(id);
            if (question == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Question not found"))
                        .build();
            }

            if (isPublic == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "isPublic parameter is required"))
                        .build();
            }

            question.setPublic(isPublic);

            if (isPublic) {
                question.setApprovalRequested(false);
            }

            QuestionDto resultDto = QuestionMapper.toDto(question);

            //System.out.println("[QuestionResource] Question " + id + " public status changed to: " + isPublic);

            return Response.ok(resultDto).build();

        } catch (NotAuthorizedException | NotFoundException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            System.err.println("[QuestionResource] Error changing public status: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error changing public status", "message", e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Delete a question by ID")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Question deleted"),
            @APIResponse(responseCode = "403", description = "Forbidden - Cannot delete this question"),
            @APIResponse(responseCode = "404", description = "Question not found")
    })
    public Response deleteQuestion(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        // System.out.println("\n[QuestionResource] Deleting question: " + id);

        try {
            User currentUser = getCurrentUser(securityContext);

            Question question = questionRepository.findById(id);
            if (question == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Question not found"))
                        .build();
            }

            if (!canModifyQuestion(currentUser, question)) {
                // System.out.println("[QuestionResource] User not allowed to delete question " + id);
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "You can only delete your own questions or public questions as admin"))
                        .build();
            }

            questionRepository.deleteQuestion(id);
            // System.out.println("[QuestionResource] Question deleted: " + id);

            return Response.noContent().build();

        } catch (NotAuthorizedException | NotFoundException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        } catch (Exception e) {
            System.err.println("[QuestionResource] Error deleting: " + e.getMessage());
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error deleting question", "message", e.getMessage()))
                    .build();
        }
    }
}