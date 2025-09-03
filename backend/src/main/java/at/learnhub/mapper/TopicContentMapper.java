package at.learnhub.mapper;

import at.learnhub.dto.simple.TopicContentSlimDto;
import at.learnhub.model.TopicContent;

public final class TopicContentMapper {
    private TopicContentMapper(){}

    public static TopicContentSlimDto toSlimDto(TopicContent tc) {
        return toSlimDto(tc, null);
    }

    public static TopicContentSlimDto toSlimDto(TopicContent tc, String uploaderNameOverride) {
        if (tc == null) return null;

        Long subjectId   = tc.getTopicPool() != null && tc.getTopicPool().getSubject() != null
                ? tc.getTopicPool().getSubject().getId() : null;
        String subjectName = tc.getTopicPool() != null && tc.getTopicPool().getSubject() != null
                ? tc.getTopicPool().getSubject().getName() : null;
        Long topicPoolId   = tc.getTopicPool() != null ? tc.getTopicPool().getId() : null;
        String topicPoolName = tc.getTopicPool() != null ? tc.getTopicPool().getName() : null;

        String uploader = uploaderNameOverride;
        if (uploader == null && tc.getCreatedBy() != null) {
            uploader = tc.getCreatedBy().getName();
        }

        String pdfUrl   = "/api/files/pdf/" + tc.getId();
        String thumbUrl = "/api/files/thumbnail/" + tc.getId();

        return new TopicContentSlimDto(
                tc.getId(),
                tc.getTitle(),
                tc.getDescription(),
                subjectId,
                subjectName,
                topicPoolId,
                topicPoolName,
                uploader,
                thumbUrl,
                pdfUrl
        );
    }
}
