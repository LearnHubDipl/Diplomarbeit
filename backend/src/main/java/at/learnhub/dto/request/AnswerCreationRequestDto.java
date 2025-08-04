package at.learnhub.dto.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "anser")
public record AnswerCreationRequestDto(
        @Schema(
                description = "",
                example = "Madrid"
        )
        String text,
        @Schema(
                description = "whether the answer is correct",
                example = "false"
        )
        Boolean isCorrect
) {

}
