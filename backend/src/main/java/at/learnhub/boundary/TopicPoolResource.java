package at.learnhub.boundary;

import at.learnhub.dto.request.CreateTopicPoolBatchRequestDto;
import at.learnhub.dto.request.CreateTopicPoolRequestDto;
import at.learnhub.dto.simple.TopicPoolSlimDto;
import at.learnhub.mapper.TopicPoolMapper;
import at.learnhub.model.Subject;
import at.learnhub.model.TopicPool;
import at.learnhub.repository.SubjectRepository;
import at.learnhub.repository.TopicPoolRepository;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.ArrayList;
import java.util.List;

@Path("/api/subjects/{subjectId}/topics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TopicPoolResource {

    @Inject TopicPoolRepository topicPoolRepo;
    @Inject SubjectRepository subjectRepo;

    @GET
    public List<TopicPoolSlimDto> listBySubject(@PathParam("subjectId") Long subjectId) {
        return topicPoolRepo.findBySubjectId(subjectId);
    }

    @POST
    public TopicPoolSlimDto createOne(@PathParam("subjectId") Long subjectId,
                                      CreateTopicPoolRequestDto dto) {
        Subject subject = subjectRepo.getById(subjectId);

        TopicPool tp = new TopicPool();
        tp.setName(dto.name());
        tp.setDescription(dto.description());
        tp.setSubject(subject);

        topicPoolRepo.create(tp);
        return TopicPoolMapper.toSlimDto(tp);
    }

    @POST
    @Path("/batch")
    public List<TopicPoolSlimDto> createBatch(@PathParam("subjectId") Long subjectId,
                                              CreateTopicPoolBatchRequestDto batch) {
        if (batch.names() == null || batch.names().isEmpty()) {
            throw new BadRequestException("names must not be empty");
        }
        if (batch.names().size() > 10) {
            throw new BadRequestException("max 10 topics per batch");
        }

        Subject subject = subjectRepo.getById(subjectId);
        List<TopicPoolSlimDto> result = new ArrayList<>(batch.names().size());

        batch.names().forEach(name -> {
            TopicPool tp = new TopicPool();
            tp.setName(name);
            tp.setSubject(subject);

            topicPoolRepo.create(tp);
            result.add(TopicPoolMapper.toSlimDto(tp));
        });

        return result;
    }
}
