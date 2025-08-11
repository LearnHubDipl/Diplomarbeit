package at.learnhub.boundary;

import at.learnhub.dto.simple.UserSlimDto;
import at.learnhub.repository.UserRepository;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/teachers")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class TeacherResource {

    @Inject UserRepository userRepo;

    @GET
    public List<UserSlimDto> getTopTeachers() {
        return userRepo.findActiveTeachers(5);
    }
}
