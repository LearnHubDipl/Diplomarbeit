package at.learnhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSubjectRequestDto(
        @NotBlank @Size(min = 2, max = 120) String name,
        @Size(max = 2000) String description,
        Long imgId // MediaFile.id (optional)
) {}