package at.learnhub.dto.request;

public record TopicContentCreateRequestDto(
        Long subjectId,
        Long topicPoolId,
        String title,
        String description,
        String uploaderName,
        Long mediaId
) {}
