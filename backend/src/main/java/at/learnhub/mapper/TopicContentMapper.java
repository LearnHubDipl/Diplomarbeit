// at.learnhub.mapper.TopicContentMapper
package at.learnhub.mapper;

import at.learnhub.dto.simple.TopicContentSlimDto;
import at.learnhub.model.TopicContent;

public class TopicContentMapper {

    private TopicContentMapper() {}

    public static TopicContentSlimDto toSlimDto(TopicContent tc) {
        if (tc == null) return null;

        Long subjectId = tc.getTopicPool() != null && tc.getTopicPool().getSubject() != null
                ? tc.getTopicPool().getSubject().getId()
                : null;
        String subjectName = tc.getTopicPool() != null && tc.getTopicPool().getSubject() != null
                ? tc.getTopicPool().getSubject().getName()
                : null;

        Long topicPoolId = tc.getTopicPool() != null ? tc.getTopicPool().getId() : null;
        String topicPoolName = tc.getTopicPool() != null ? tc.getTopicPool().getName() : null;

        String uploaderName = tc.getCreatedBy() != null ? tc.getCreatedBy().getName() : null;

        String thumbnailUrl = tc.getMedia() != null && tc.getMedia().getId() != null
                ? "/api/files/thumbnail/" + tc.getMedia().getId()
                : null;

        String pdfUrl = tc.getMedia() != null && tc.getMedia().getId() != null
                ? "/api/files/pdf/" + tc.getMedia().getId()
                : null;

        return new TopicContentSlimDto(
                tc.getId(),
                subjectId,
                subjectName,
                topicPoolId,
                topicPoolName,
                uploaderName,
                thumbnailUrl,
                pdfUrl
        );
    }
}
