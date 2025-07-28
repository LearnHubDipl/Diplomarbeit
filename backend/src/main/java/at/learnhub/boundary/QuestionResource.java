package at.learnhub.boundary;

import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.repository.QuestionRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/questions")
public class QuestionResource {
    @Inject
    QuestionRepository questionRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllQuestions() {
        List<QuestionDto> questions = questionRepository.findAll();
        return Response.ok(questions).build();
    }
}
