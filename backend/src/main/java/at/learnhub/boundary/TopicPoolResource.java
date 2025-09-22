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
import jakarta.transaction.Transactional;
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
    @Transactional
    public TopicPoolSlimDto createOne(@PathParam("subjectId") Long subjectId,
                                      CreateTopicPoolRequestDto dto) {
        Subject subject = subjectRepo.getById(subjectId);
        if (subject == null) throw new NotFoundException("Subject not found");
        TopicPool tp = new TopicPool();
        tp.setName(dto.name());
        tp.setDescription(dto.description());
        tp.setSubject(subject);
        topicPoolRepo.create(tp);
        return TopicPoolMapper.toSlimDto(tp);
    }

    @POST
    @Path("/batch")
    @Transactional
    public List<TopicPoolSlimDto> createBatch(@PathParam("subjectId") Long subjectId,
                                              CreateTopicPoolBatchRequestDto batch) {
        if (batch.names() == null || batch.names().isEmpty()) {
            throw new BadRequestException("names must not be empty");
        }
        Subject subject = subjectRepo.getById(subjectId);
        if (subject == null) throw new NotFoundException("Subject not found");

        List<TopicPoolSlimDto> result = new ArrayList<>();
        for (String name : batch.names()) {
            TopicPool tp = new TopicPool();
            tp.setName(name);
            tp.setSubject(subject);
            topicPoolRepo.create(tp);
            result.add(TopicPoolMapper.toSlimDto(tp));
        }
        return result;
    }

    @PUT
    @Path("/{topicPoolId}")
    @Transactional
    public TopicPoolSlimDto updateOne(@PathParam("topicPoolId") Long topicPoolId,
                                      CreateTopicPoolRequestDto dto) {
        TopicPool tp = topicPoolRepo.getTopicPoolById(topicPoolId);
        if (tp == null) throw new NotFoundException("TopicPool not found");

        if (dto.name() != null && !dto.name().isBlank()) {
            tp.setName(dto.name().trim());
        }
        if (dto.description() != null) {
            tp.setDescription(dto.description());
        }
        topicPoolRepo.update(tp);
        return TopicPoolMapper.toSlimDto(tp);
    }

    @DELETE
    @Path("/{topicPoolId}")
    @Transactional
    public void deleteOne(@PathParam("subjectId") Long subjectId,
                          @PathParam("topicPoolId") Long topicPoolId) {
        TopicPool tp = topicPoolRepo.getTopicPoolById(topicPoolId);
        if (tp == null || tp.getSubject() == null || !tp.getSubject().getId().equals(subjectId)) {
            throw new NotFoundException("TopicPool not found for subject " + subjectId);
        }
        topicPoolRepo.delete(topicPoolId);
    }
}