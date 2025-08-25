package at.learnhub.dto.response;

import at.learnhub.dto.simple.QuestionSlimDto;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Response payload containing the details of a newly created exam")
public record CreatedExamResponseDto(

        @Schema(description = "Unique identifier of the created exam",
                example = "101")
        Long examId,

        @Schema(description = "Time limit for the exam in minutes",
                example = "60")
        Integer timeLimitMinutes,

        @Schema(description = "Timestamp when the exam was started",
                example = "2025-07-10T14:30:00")
        LocalDateTime startedAt,

        @Schema(description = "Total number of questions included in this exam",
                example = "20")
        Integer questionCount,

        @Schema(description = "The list of exam-specific questions that were presented to the user",
                type = SchemaType.ARRAY,
                implementation = QuestionSlimDto.class)
        List<QuestionSlimDto> questions
) {
}
