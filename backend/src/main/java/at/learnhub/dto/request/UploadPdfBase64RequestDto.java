package at.learnhub.dto.request;

public record UploadPdfBase64RequestDto(
        String base64,
        String title,
        String description,
        String uploaderName,
        Long teacherId,
        Long subjectId,
        Long topicPoolId
) {}
