package at.learnhub.dto.request;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = """
        Request to check if the user's answer is correct.
        Exactly one of 'selectedAnswerIds' (for multiple choice questions) or 'freeTextAnswer' (for freetext questions) must be provided.
        """)
public record CheckAnswersRequestDto(

        @Schema(
                description = "The ID of the question being evaluated",
                example = "4",
                required = true
        )
        Long questionId,

        @Schema(
                description = "List of answer IDs selected by the user. Required only for multiple choice questions. Must be null for freetext questions.",
                example = "[101, 102]",
                type = SchemaType.ARRAY
        )
        List<Long> selectedAnswerIds,

        @Schema(
                description = "Answer the user provided for a freetext question. Required only for freetext questions. Must be null for multiple choice questions.",
                example = "The capital city of France is Paris."
        )
        String freeTextAnswer

) {
}
