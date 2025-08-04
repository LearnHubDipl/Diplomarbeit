package at.learnhub.dto.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Dto for creating answers, containing text and if the answer is correct")
public record AnswerCreationRequestDto(
        @Schema(
                description = "The text of one Answer Option",
                example = "Madrid"
        )
        String text,
        @Schema(
                description = "Whether the answer option is correct",
                example = "false"
        )
        Boolean isCorrect
) {
}
