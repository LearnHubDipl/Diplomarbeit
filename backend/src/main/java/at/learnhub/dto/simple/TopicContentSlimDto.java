package at.learnhub.dto.simple;

public record TopicContentSlimDto(
        Long id,
        Long subjectId,
        String subjectName,
        Long topicPoolId,
        String topicPoolName,
        String uploaderName, // aus createdBy.name
        String thumbnailUrl, // z. B. /api/files/thumbnail/{id}
        String pdfUrl        // optional: /api/files/pdf/{id} oder aus MediaFile.path abgeleitet
) {}
