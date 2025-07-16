package at.learnhub.dto.request;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Request to check if selected answers for a given question are correct.")
public record CheckAnswersRequestDto(

        @Schema(
                description = "The ID of the question being evaluated",
                example = "4",
                required = true
        )
        Long questionId,

        @Schema(
                description = "List of answer IDs selected by the user",
                example = "[101, 102]",
                type = SchemaType.ARRAY,
                required = true
        )
        List<Long> selectedAnswerIds

) {
}
