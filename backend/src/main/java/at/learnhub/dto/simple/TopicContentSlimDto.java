package at.learnhub.dto.simple;

public record TopicContentSlimDto(
        Long id,
        String title,
        String description,
        Long subjectId,
        String subjectName,
        Long topicPoolId,
        String topicPoolName,
        String uploaderName,
        String thumbnailUrl,
        String pdfUrl
) {}

