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
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
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

    private CustomSecurityContext extractCustomSecurityContext(SecurityContext securityContext) {
        if (securityContext == null) return null;
        if (securityContext instanceof CustomSecurityContext) return (CustomSecurityContext) securityContext;
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

    private User getCurrentUser(SecurityContext securityContext) {
        if (securityContext == null) throw new NotAuthorizedException("No security context");

        CustomSecurityContext customContext = extractCustomSecurityContext(securityContext);
        String keycloakSub = null;

        if (customContext != null) {
            keycloakSub = customContext.keycloakSub();
        } else {
            Principal principal = securityContext.getUserPrincipal();
            if (principal != null) keycloakSub = principal.getName();
        }

        if (keycloakSub == null) throw new NotAuthorizedException("No user principal found");

        Optional<User> userOptional = userRepository.findUserEntityByKeycloakSub(keycloakSub);
        if (userOptional.isEmpty()) {
            System.err.println("[getCurrentUser] User not found for keycloakSub: " + keycloakSub);
            throw new NotFoundException("User not found in database");
        }

        return userOptional.get();
    }

    private boolean canModifyQuestion(User currentUser, Question question) {
        if ((Boolean.TRUE.equals(currentUser.getAdmin()) || Boolean.TRUE.equals(currentUser.getTeacher()))
                && Boolean.TRUE.equals(question.getPublic())) return true;
        return question.getUser().getId().equals(currentUser.getId());
    }

    @GET
    @Operation(summary = "List all questions")
    public Response getAllQuestions(@Context SecurityContext securityContext) {
        try {
            List<QuestionDto> questions = questionRepository.findAll();
            return Response.ok(questions).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error loading questions", "message", e.getMessage())).build();
        }
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get a single question by ID")
    public Response getQuestionById(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        try {
            QuestionDto question = questionRepository.getQuestionDtoById(id);
            return Response.ok(question).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Question not found", "id", id)).build();
        }
    }

    @GET
    @Path("/byTopicPool/{topicPoolId}")
    @Operation(summary = "Get questions for a topic pool")
    public Response getQuestionsByTopicPool(@PathParam("topicPoolId") Long topicPoolId,
                                            @Context SecurityContext securityContext) {
        try {
            List<QuestionDto> questions = questionService.getQuestionsByTopicPool(topicPoolId);
            return Response.ok(questions).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error loading questions", "message", e.getMessage())).build();
        }
    }

    @GET
    @Path("/user/{userId}")
    @Operation(summary = "Get questions from a specific user")
    public Response getQuestionsByUserId(@PathParam("userId") Long userId,
                                         @Context SecurityContext securityContext) {
        try {
            List<QuestionDto> questions = questionRepository.findByUserId(userId);
            return Response.ok(questions).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error loading questions", "message", e.getMessage())).build();
        }
    }

    @GET
    @Path("/public")
    @Operation(summary = "Get all public questions")
    public Response getPublicQuestions(@Context SecurityContext securityContext) {
        try {
            List<QuestionDto> questions = questionRepository.findAllPublicQuestions();
            return Response.ok(questions).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error loading questions", "message", e.getMessage())).build();
        }
    }

    @GET
    @Path("/private")
    @Operation(summary = "Get all private questions (admin only)")
    public Response getAllPrivateQuestions(@Context SecurityContext securityContext) {
        try {
            User currentUser = getCurrentUser(securityContext);

            if (!Boolean.TRUE.equals(currentUser.getAdmin()) && !Boolean.TRUE.equals(currentUser.getTeacher())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "Only admins can view all private questions")).build();
            }

            List<QuestionDto> questions = questionRepository.findAllPrivateQuestions();
            return Response.ok(questions).build();

        } catch (NotAuthorizedException | NotFoundException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error loading private questions", "message", e.getMessage())).build();
        }
    }

    @GET
    @Path("/awaiting-approval")
    @Operation(summary = "Get all questions awaiting approval (admin and teachers)")
    public Response findAllQuestionsWithApprovalRequested(@Context SecurityContext securityContext) {
        try {
            User currentUser = getCurrentUser(securityContext);

            if (!Boolean.TRUE.equals(currentUser.getAdmin()) && !Boolean.TRUE.equals(currentUser.getTeacher())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "Only admins and teachers can view questions awaiting approval"))
                        .build();
            }

            List<QuestionDto> questions = questionRepository.findAllQuestionsWithApprovalRequested();
            return Response.ok(questions).build();

        } catch (NotAuthorizedException | NotFoundException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error loading questions", "message", e.getMessage())).build();
        }
    }

    @GET
    @Path("/type/{type}")
    @Operation(summary = "Get questions by type")
    public Response getQuestionsByType(@PathParam("type") String type,
                                       @Context SecurityContext securityContext) {
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
    public Response getQuestionsByDifficulty(@PathParam("level") Integer level,
                                             @Context SecurityContext securityContext) {
        List<QuestionDto> questions = questionRepository.findByDifficulty(level);
        return Response.ok(questions).build();
    }

    @GET
    @Path("/ids")
    @Operation(summary = "Get only question IDs")
    public Response getQuestionIds(@QueryParam("topicPoolId") Long topicPoolId,
                                   @QueryParam("userId") Long userId,
                                   @Context SecurityContext securityContext) {
        if (userId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "User ID is required")).build();
        }

        try {
            List<Long> ids = questionService.getQuestionIds(topicPoolId, userId);
            return Response.ok(ids).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error getting question IDs", "message", e.getMessage())).build();
        }
    }

    @POST
    @Transactional
    @Operation(summary = "Create a new question")
    public Response createQuestion(@RequestBody(required = true) QuestionCreationRequestDto dto,
                                   @Context SecurityContext securityContext) {
        try {
            User currentUser = getCurrentUser(securityContext);

            if (dto.text() == null || dto.text().isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Question text is required")).build();
            }
            if (dto.userId() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "User ID is required")).build();
            }
            if (dto.topicPoolId() == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "Topic pool ID is required")).build();
            }
            if (Boolean.TRUE.equals(dto.isPublic()) && !Boolean.TRUE.equals(currentUser.getAdmin()) && !Boolean.TRUE.equals(currentUser.getTeacher())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "Only admins can create public questions")).build();
            }
            if (!dto.userId().equals(currentUser.getId())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "You can only create questions for yourself")).build();
            }

            QuestionDto resultDto = questionService.create(dto);
            return Response.status(Response.Status.CREATED).entity(resultDto).build();

        } catch (NotAuthorizedException | NotFoundException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error creating question", "message", e.getMessage())).build();
        }
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Update an existing question")
    public Response updateQuestion(@PathParam("id") Long id,
                                   @RequestBody QuestionUpdateRequestDto dto,
                                   @Context SecurityContext securityContext) {
        try {
            User currentUser = getCurrentUser(securityContext);

            Question question = questionRepository.findById(id);
            if (question == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Question not found")).build();
            }
            if (!canModifyQuestion(currentUser, question)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "You can only modify your own questions or public questions as admin"))
                        .build();
            }
            if (dto.isPublic() != null && dto.isPublic()
                    && !Boolean.TRUE.equals(currentUser.getAdmin())
                    && !Boolean.TRUE.equals(currentUser.getTeacher())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "Only admins or teachers can make questions public")).build();
            }

            Question updatedQuestion = questionRepository.updateQuestion(id, dto);
            return Response.ok(QuestionMapper.toDto(updatedQuestion)).build();

        } catch (NotAuthorizedException | NotFoundException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error updating question", "message", e.getMessage())).build();
        }
    }

    @PATCH
    @Path("/{id}/public")
    @Transactional
    @Operation(summary = "Change public status of a question (admin only)")
    public Response changeQuestionPublicStatus(@PathParam("id") Long id,
                                               @QueryParam("isPublic") @Parameter(required = true) Boolean isPublic,
                                               @Context SecurityContext securityContext) {
        try {
            User currentUser = getCurrentUser(securityContext);

            if (!Boolean.TRUE.equals(currentUser.getAdmin()) && !Boolean.TRUE.equals(currentUser.getTeacher())) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "Only admins can change public status of questions")).build();
            }

            Question question = questionRepository.findById(id);
            if (question == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Question not found")).build();
            }
            if (isPublic == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "isPublic parameter is required")).build();
            }

            question.setPublic(isPublic);
            if (isPublic) question.setApprovalRequested(false);

            return Response.ok(QuestionMapper.toDto(question)).build();

        } catch (NotAuthorizedException | NotFoundException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error changing public status", "message", e.getMessage())).build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Delete a question by ID")
    public Response deleteQuestion(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        try {
            User currentUser = getCurrentUser(securityContext);

            Question question = questionRepository.findById(id);
            if (question == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(Map.of("error", "Question not found")).build();
            }
            if (!canModifyQuestion(currentUser, question)) {
                return Response.status(Response.Status.FORBIDDEN)
                        .entity(Map.of("error", "You can only delete your own questions or public questions as admin"))
                        .build();
            }

            questionRepository.deleteQuestion(id);
            return Response.noContent().build();

        } catch (NotAuthorizedException | NotFoundException e) {
            return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", e.getMessage())).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error deleting question", "message", e.getMessage())).build();
        }
    }
}