package at.learnhub.boundary;

import at.learnhub.model.Subject;
import at.learnhub.repository.SubjectRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/subject/")
public class SubjectResource {
    @Inject
    SubjectRepository subjectRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("list")
    public Response getSubjectList() {
        List<Subject> subjects = subjectRepository.findAll();

        return Response.status(Response.Status.OK).entity(subjects).build();
    }
}
