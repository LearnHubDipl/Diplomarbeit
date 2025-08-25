package at.learnhub.boundary;

import at.learnhub.dto.request.CreateExamRequestDto;
import at.learnhub.dto.request.SubmitExamRequestDto;
import at.learnhub.dto.response.CreatedExamResponseDto;
import at.learnhub.dto.response.SubmittedExamResponseDto;
import at.learnhub.dto.simple.ExamDto;
import at.learnhub.service.ExamService;
import at.learnhub.repository.ExamRepository;
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

@Path("/api/exams")
@Produces(MediaType.APPLICATION_JSON)
public class ExamResource {

    @Inject
    ExamService examService;

    @Inject
    ExamRepository examRepository;

    @GET
    @Operation(
            summary = "List all exams",
            description = "Returns a list of all exams as DTOs."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "List of exams retrieved successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(
                                    type = SchemaType.ARRAY,
                                    implementation = ExamDto.class
                            )
                    )
            )
    })
    public Response getAllExams() {
        List<ExamDto> exams = examRepository.findAll();
        return Response.ok(exams).build();
    }

    @GET
    @Path("/{id}")
    @Operation(
            summary = "Get a single exam by ID",
            description = "Returns a single Exam DTO for the given ID"
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Exam found",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ExamDto.class)
                    )
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Exam not found"
            )
    })
    public Response getById(
            @Parameter(
                    description = "ID of the exam to fetch",
                    required = true,
                    example = "101"
            )
            @PathParam("id") Long id
    ) {
        ExamDto exam = examRepository.getDtoById(id);
        return Response.ok(exam).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(
            summary = "Create a new exam",
            description = "Creates a new exam based on the provided request details and returns the created exam."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "201",
                    description = "Exam created successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = CreatedExamResponseDto.class)
                    )
            ),
            @APIResponse(
                    responseCode = "400",
                    description = "Invalid request data"
            )
    })
    @Path("/create")
    public Response createExam(
            @RequestBody(
                    required = true,
                    description = "Payload containing the details required to create a new exam",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = CreateExamRequestDto.class)
                    )
            )
            CreateExamRequestDto request
    ) {
        CreatedExamResponseDto response = examService.createExam(request);
        return Response.status(Response.Status.CREATED).entity(response).build();
    }

    @POST
    @Path("/submit")
    @Operation(
            summary = "Submit answers for an exam",
            description = "Evaluates the submitted answers, persists results, and returns the grading outcome."
    )
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Exam submitted and graded successfully",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SubmittedExamResponseDto.class)
                    )
            ),
            @APIResponse(responseCode = "404", description = "Exam or question not found"),
            @APIResponse(responseCode = "400", description = "Invalid submission data")
    })
    public Response submitExam(
            @RequestBody(
                    description = "Answers to submit for the exam",
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = SubmitExamRequestDto.class)
                    )
            )
            SubmitExamRequestDto request
    ) {
        SubmittedExamResponseDto response = examService.submitExam(request);
        return Response.ok(response).build();
    }
}
