// at.learnhub.boundary/TopicContentResource.java
package at.learnhub.boundary;

import at.learnhub.dto.request.TopicContentCreateRequestDto;
import at.learnhub.dto.simple.TopicContentSlimDto;
import at.learnhub.mapper.TopicContentMapper;
import at.learnhub.model.MediaFile;
import at.learnhub.model.Subject;
import at.learnhub.model.TopicContent;
import at.learnhub.model.TopicPool;
import at.learnhub.repository.MediaFileRepository;
import at.learnhub.repository.SubjectRepository;
import at.learnhub.repository.TopicContentRepository;
import at.learnhub.repository.TopicPoolRepository;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.util.List;

@Path("/api/topic-contents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TopicContentResource {

    @Inject TopicContentRepository topicContentRepo;
    @Inject SubjectRepository subjectRepo;
    @Inject TopicPoolRepository topicPoolRepo;
    @Inject MediaFileRepository mediaRepo;

    @GET
    public List<TopicContentSlimDto> list(@QueryParam("subjectId") Long subjectId,
                                          @QueryParam("topicPoolId") Long topicPoolId,
                                          @QueryParam("includeUnapproved") @DefaultValue("false") boolean includeUnapproved) {
        if (subjectId == null) {
            throw new BadRequestException("subjectId is required");
        }
        List<TopicContent> list;
        if (topicPoolId != null) {
            list = includeUnapproved
                    ? topicContentRepo.findBySubjectAndTopicAll(subjectId, topicPoolId)
                    : topicContentRepo.findApprovedBySubjectAndTopic(subjectId, topicPoolId);
        } else {
            list = includeUnapproved
                    ? topicContentRepo.findBySubjectAll(subjectId)
                    : topicContentRepo.findApprovedBySubject(subjectId);
        }
        return list.stream()
                .map(tc -> TopicContentMapper.toSlimDto(tc))
                .collect(Collectors.toList());
    }

    @POST
    @Transactional
    public Response create(TopicContentCreateRequestDto dto) {
        if (dto == null || dto.subjectId() == null || dto.title() == null || dto.mediaId() == null) {
            throw new BadRequestException("subjectId, title, mediaId are required");
        }
        Subject s = subjectRepo.getById(dto.subjectId());
        TopicPool tp = null;
        if (dto.topicPoolId() != null) {
            tp = topicPoolRepo.getTopicPoolById(dto.topicPoolId());
            if (!tp.getSubject().getId().equals(s.getId())) {
                throw new BadRequestException("topicPool does not belong to subject");
            }
        }
        MediaFile mf = mediaRepo.getById(dto.mediaId());

        TopicContent tc = new TopicContent();
        tc.setTitle(dto.title());
        tc.setDescription(dto.description());
        tc.setDate(LocalDate.now());
        tc.setMedia(mf);
        tc.setTopicPool(tp);
        tc.setApproved(true);
        topicContentRepo.persist(tc);

        return Response.status(Response.Status.CREATED)
                .entity(TopicContentMapper.toSlimDto(tc, dto.uploaderName()))
                .build();
    }
}
