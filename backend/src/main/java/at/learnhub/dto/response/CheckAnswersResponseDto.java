package at.learnhub.dto.response;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = """
        Response indicating whether the provided answer(s) to a question are correct.
        Either 'correctAnswerIds' (for multiple choice questions) or 'correctFreeTextAnswers' (for freetext questions) will be populated — not both.
        """)
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
                description = "List of the correct answer IDs for the question. Only populated for multiple choice questions. Will be null for freetext questions.",
                example = "[101, 102]",
                type = SchemaType.ARRAY
        )
        List<Long> correctAnswerIds,

        @Schema(
                description = "List of accepted free text answers. Only populated for freetext questions. Will be null for multiple choice questions.",
                example = "[\"Paris\", \"The capital of France is Paris\"]",
                type = SchemaType.ARRAY
        )
        List<String> correctFreeTextAnswers,

        @Schema(
                description = "Explanation of the correct answers (for feedback)",
                example = "Answers 101 and 102 are correct because they explain all required concepts."
        )
        String explanation

) {
}
