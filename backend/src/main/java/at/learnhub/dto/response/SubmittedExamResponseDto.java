package at.learnhub.dto.response;

import at.learnhub.dto.simple.ExamQuestionSlimDto;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response after submitting an exam, including grading result.")
public record SubmittedExamResponseDto(

        @Schema(description = "ID of the submitted exam", example = "1")
        Long examId,

        @Schema(description = "Total score achieved in the exam", example = "87.5")
        Double score,

        @Schema(description = "List of exam questions including the evaluation result",
                type = SchemaType.ARRAY,
                implementation = ExamQuestionSlimDto.class)
        List<ExamQuestionSlimDto> questions

) {}
