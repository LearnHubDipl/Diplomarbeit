package at.learnhub.boundary;

import at.learnhub.repository.TeacherRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/api/teachers")
@Produces(MediaType.APPLICATION_JSON)
public class TeacherResource {

    public record TeacherDto(Long id, String name) {}

    @Inject TeacherRepository repo;

    @GET
    public List<TeacherDto> list() {
        return repo.listAll().stream()
                .map(t -> new TeacherDto(t.getId(), t.getName()))
                .toList();
    }
}
