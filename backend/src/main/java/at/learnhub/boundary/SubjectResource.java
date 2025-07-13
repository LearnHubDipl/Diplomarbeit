package at.learnhub.boundary;

import at.learnhub.dto.SubjectDto;
import at.learnhub.repository.SubjectRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Path("/api/subject/")
public class SubjectResource {
    @Inject
    SubjectRepository subjectRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("list")
    @Operation(summary = "List all subjects", description = "Returns a list of all subjects as DTOs")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "List of subjects",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    implementation = SubjectDto.class,
                                    type = SchemaType.ARRAY
                            )
                    )
            )
    })
    public Response getSubjectList() {
        List<SubjectDto> subjects = subjectRepository.findAll();
        return Response.ok(subjects).build();
    }
}
