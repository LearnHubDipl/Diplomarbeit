package at.learnhub.dto.simple;

public record UploadPdfResult(
        Long contentId,
        String publicUrl,
        String thumbUrl
) {}
