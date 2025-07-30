package at.learnhub.dto.simple;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "A simplified Dto representing an answer option for a question")
public record AnswerSlimDto(
        @Schema(
                description = "Unique identifier of the answer.",
                example = "3",
                readOnly = true
        )
        Long id,

        @Schema(
                description = "The answer from the question shown to the user.",
                example = "Paris"
        )
        String text,

        @Schema(
                description = "Indicates whether this answer is correct.",
                example = "true"
        )
        Boolean isCorrect
) {
}