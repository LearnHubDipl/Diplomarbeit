package at.learnhub.boundary;

import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.dto.simple.SubjectDto;
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
}
