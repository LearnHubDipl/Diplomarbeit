package at.learnhub.boundary;

import at.learnhub.dto.simple.TopicContentSlimDto;
import at.learnhub.mapper.TopicContentMapper;
import at.learnhub.repository.TopicContentRepository;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/topic-contents")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class TopicContentResource {

    @Inject TopicContentRepository topicContentRepo;

    @GET
    public List<TopicContentSlimDto> getApproved(
            @QueryParam("subjectId") Long subjectId,
            @QueryParam("topicPoolId") Long topicPoolId) {

        if (subjectId != null && topicPoolId != null) {
            return topicContentRepo.findApprovedBySubjectAndTopic(subjectId, topicPoolId);
        } else if (subjectId != null) {
            return topicContentRepo.findApprovedBySubject(subjectId);
        } else {
            throw new BadRequestException("subjectId is required");
        }
    }
}
