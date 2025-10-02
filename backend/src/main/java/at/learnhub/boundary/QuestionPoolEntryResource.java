package at.learnhub.boundary;

import at.learnhub.model.QuestionPoolEntry;
import at.learnhub.service.QuestionPoolEntryService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.util.List;

@Path("/api/entries")
public class QuestionPoolEntryResource {

    @Inject
    QuestionPoolEntryService service;


    @POST
    @Path("/increase-correct-count")
    @Operation(
            summary = "Increase correct count",
            description = "Increments the correctCount of a QuestionPoolEntry for a given question and user"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "204",
                    description = "Correct count successfully increased"
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Missing or invalid parameters"
            )
    })
    public Response increaseCorrectCount(
            @Parameter(
                    description = "Question ID to increment correctCount for",
                    required = true,
                    example = "123"
            )
            @QueryParam("questionId") Long questionId,
            @Parameter(
                    description = "User ID whose entry should be updated",
                    required = true,
                    example = "1"
            )
            @QueryParam("userId") Long userId
    ) {
        if (questionId == null || userId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("questionId and userId are required")
                    .build();
        }

        service.increaseCorrectCount(questionId, userId);
        return Response.noContent().build();
    }

    @GET
    @Path("/all")
    public Response getAllEntries(
            @QueryParam("userId") Long userId,
            @QueryParam("topicPoolId") Long topicPoolId
    ) {
        if (userId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("userId query param is required")
                    .build();
        }

        List<QuestionPoolEntry> entries = service.getAllEntries(userId, topicPoolId);
        return Response.ok(entries).build();
    }

}
