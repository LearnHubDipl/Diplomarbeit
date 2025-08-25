package at.learnhub.dto.request;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Request payload for submitting answers to an exam.")
public record SubmitExamRequestDto(

        @Schema(description = "ID of the exam being submitted", example = "1")
        Long examId,

        @Schema(description = "List of answered questions",
                type = SchemaType.ARRAY,
                implementation = CheckAnswersRequestDto.class,
                example = """
                        [
                          {"questionId": 1, "selectedAnswerIds": [3], "freeTextAnswer": null},
                          {"questionId": 2, "selectedAnswerIds": null, "freeTextAnswer": "Das ist eine falsche Antwort."},
                          {"questionId": 3, "selectedAnswerIds": [7, 8], "freeTextAnswer": null}
                        ]
                        """
        )
        List<CheckAnswersRequestDto> answers

) {
}
