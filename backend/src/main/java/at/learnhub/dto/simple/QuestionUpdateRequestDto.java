package at.learnhub.dto.simple;

import at.learnhub.model.QuestionType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "dto for updating an existing question.")
public record QuestionUpdateRequestDto(
        @Schema(description = "Updated text of the question", example = "What is the capital of Austria?")
        String text,

        @Schema(description = "Updated explanation of the question", example = "Vienna is the capital of AUT")
        String explanation,

        @Schema(description = "Updated question type", example = "MULTIPLE_CHOICE")
        QuestionType type,

        @Schema(description = "Updated list of answers")
        List<AnswerUpdateRequestDto> answers,

        @Schema(description = "Whether the question is public or not", example = "true")
        Boolean isPublic,

        @Schema(description = "Updated approval request status")
        Boolean approvalRequested
) {
}
