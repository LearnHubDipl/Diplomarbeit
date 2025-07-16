package at.learnhub.dto.response;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Request to check if selected answers for a given question are correct.")
public record CheckAnswersResponseDto(

        @Schema(
                description = "The ID of the question being evaluated",
                example = "4"
        )
        Long questionId,

        @Schema(
                description = "Whether the submitted answers are correct",
                example = "true"
        )
        Boolean correct,

        @Schema(
                description = "List of the correct answer IDs for the question",
                example = "[101, 102]",
                type = SchemaType.ARRAY
        )
        List<Long> correctAnswerIds,

        @Schema(
                description = "Explanation of the correct answers (for feedback)",
                example = "Answers 101 and 102 are correct because they explain all required concepts."
        )
        String explanation

) {
}
