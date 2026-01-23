package at.learnhub.mapper;

import at.learnhub.dto.response.TopicContentDto;
import at.learnhub.dto.simple.TopicContentSlimDto;
import at.learnhub.model.TopicContent;
import at.learnhub.model.User;

public final class TopicContentMapper {

    private TopicContentMapper() {}

    public static TopicContentDto toDto(TopicContent tc) {
        if (tc == null) return null;

        Long subjectId   = (tc.getTopicPool()!=null && tc.getTopicPool().getSubject()!=null)
                ? tc.getTopicPool().getSubject().getId() : null;
        Long topicPoolId = (tc.getTopicPool()!=null ? tc.getTopicPool().getId() : null);

        String uploaderName = tc.getCreatedBy() != null ? safeName(tc.getCreatedBy()) : null;
        Long teacherId      = tc.getTaughtBy() != null ? tc.getTaughtBy().getId() : null;
        String teacherName  = tc.getTaughtBy() != null ? safeName(tc.getTaughtBy()) : null;

        String pdfUrl = (tc.getMedia()!=null ? tc.getMedia().getPath() : null);

        return new TopicContentDto(
                tc.getId(),
                tc.getTitle(),
                tc.getDescription(),
                uploaderName,
                tc.getDate(),
                teacherId,
                teacherName,
                pdfUrl,
                subjectId,
                topicPoolId,
                tc.getApproved()
        );
    }

    public static TopicContentSlimDto toSlimDto(TopicContent tc) {
        return toSlimDto(tc, null);
    }

    public static TopicContentSlimDto toSlimDto(TopicContent tc, String uploaderNameOverride) {
        if (tc == null) return null;

        Long subjectId     = (tc.getTopicPool()!=null && tc.getTopicPool().getSubject()!=null)
                ? tc.getTopicPool().getSubject().getId() : null;
        String subjectName = (tc.getTopicPool()!=null && tc.getTopicPool().getSubject()!=null)
                ? tc.getTopicPool().getSubject().getName() : null;

        Long topicPoolId     = (tc.getTopicPool()!=null ? tc.getTopicPool().getId() : null);
        String topicPoolName = (tc.getTopicPool()!=null ? tc.getTopicPool().getName() : null);

        String uploaderName = (uploaderNameOverride != null)
                ? uploaderNameOverride
                : (tc.getCreatedBy()!=null ? safeName(tc.getCreatedBy()) : null);

        String pdfUrl       = (tc.getMedia()!=null ? tc.getMedia().getPath() : null);
        String thumbnailUrl = null;

        return new TopicContentSlimDto(
                tc.getId(),
                tc.getTitle(),
                tc.getDescription(),
                subjectId,
                subjectName,
                topicPoolId,
                topicPoolName,
                uploaderName,
                thumbnailUrl,
                pdfUrl
        );
    }

    private static String safeName(User u) {
        if (u == null) return null;
        try {
            return u.getName();
        } catch (Throwable ignored) {
            return null;
        }
    }
}
