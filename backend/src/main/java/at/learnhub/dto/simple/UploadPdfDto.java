package at.learnhub.dto.simple;

public record UploadPdfDto(
        String title,
        Long subjectId,
        Long topicPoolId,
        Long uploaderUserId,
        String base64,
        String fileName
) {}