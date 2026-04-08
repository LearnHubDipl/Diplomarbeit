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
import jakarta.ws.rs.core.Response;

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
    public Response updateOne(@PathParam("subjectId") Long subjectId,
                              @PathParam("topicPoolId") Long topicPoolId,
                              CreateTopicPoolRequestDto dto) {

        // Atomarer belongs-to-Check (Repository siehe unten)
        TopicPool tp = topicPoolRepo.getByIdAndSubject(topicPoolId, subjectId);
        if (tp == null) throw new NotFoundException(
                "TopicPool %d not found for subject %d".formatted(topicPoolId, subjectId));

        if (dto.name() != null && !dto.name().isBlank()) {
            tp.setName(dto.name().trim());
        }
        if (dto.description() != null) {
            tp.setDescription(dto.description());
        }

        topicPoolRepo.update(tp);
        return Response.ok(TopicPoolMapper.toSlimDto(tp)).build(); // 200 + aktualisiertes Objekt
    }
    @DELETE
    @Path("/{topicPoolId}")
    @Transactional
    public Response deleteOne(@PathParam("subjectId") Long subjectId,
                              @PathParam("topicPoolId") Long topicPoolId) {
        int affected = topicPoolRepo.deleteByIdAndSubject(topicPoolId, subjectId);
        if (affected == 0) {
            throw new NotFoundException(
                    "TopicPool %d not found for subject %d".formatted(topicPoolId, subjectId));
        }
        return Response.noContent().build();
    }
}