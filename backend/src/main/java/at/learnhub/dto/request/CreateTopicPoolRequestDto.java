package at.learnhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTopicPoolRequestDto(
        @NotBlank @Size(min = 2, max = 120) String name,
        @Size(max = 2000) String description
) {}
