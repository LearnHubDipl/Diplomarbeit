package at.learnhub.dto.simple;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record AnswerSlimDto(
        @Schema(
                readOnly = true
        )
        Long id,

        @Schema(
        )
        String text,

        @Schema(
                example = "true"
        )
        Boolean isCorrect
) {
}