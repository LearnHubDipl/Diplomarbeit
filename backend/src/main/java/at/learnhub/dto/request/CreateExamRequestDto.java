package at.learnhub.dto.request;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;


@Schema(description = "Request payload for creating a new exam session")
public record CreateExamRequestDto(

        @Schema(description = "Unique identifier of the user who is taking the exam",
                example = "1",
                required = true)
        Long userId,

        @Schema(description = "List of topic pool IDs from which exam questions will be selected",
                example = "[1, 2, 3]",
                type = SchemaType.ARRAY)
        List<Long> topicPoolIds,

        @Schema(description = "Total number of questions to include in the exam",
                example = "6",
                minimum = "1")
        int numberOfQuestions,

        @Schema(description = "Time limit for the exam in minutes",
                example = "60",
                minimum = "1")
        int timeLimitMinutes
) {}
