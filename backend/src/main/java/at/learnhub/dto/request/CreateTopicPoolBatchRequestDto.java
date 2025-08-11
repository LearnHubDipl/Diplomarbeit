package at.learnhub.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateTopicPoolBatchRequestDto(
        @NotEmpty List<@Size(min = 2, max = 120) String> names // max. 10 serverseitig prüfen
) {}
