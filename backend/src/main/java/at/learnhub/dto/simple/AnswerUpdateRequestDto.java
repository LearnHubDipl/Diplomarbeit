package at.learnhub.dto.simple;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "dto for updated answers")
public record AnswerUpdateRequestDto(
        @Schema(description = "id of the answer", example = "42")
        Long id,

        @Schema(description = "text of the answer", example = "Berlin")
        String text,

        @Schema(description = "Whether this answer is correct", example = "false")
        Boolean isCorrect
) {
}
