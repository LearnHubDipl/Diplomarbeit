package at.learnhub.dto.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record CreateMediaRequestDto(
        String path,
        String type,
        String description
) {}
