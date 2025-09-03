package at.learnhub.dto.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record CreatePdfFromBaseRequestDto(
        String fileName,
        String base64,
        String description
) {}
