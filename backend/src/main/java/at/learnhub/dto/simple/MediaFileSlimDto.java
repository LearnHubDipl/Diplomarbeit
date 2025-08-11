package at.learnhub.dto.simple;
import java.time.LocalDateTime;

public record MediaFileSlimDto(
        Long id,
        String path,
        String type,
        String description,
        LocalDateTime uploadedAt
) {}
