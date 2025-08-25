package at.learnhub.dto.simple;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Slim representation of an exam question, containing question details, selected answers and evaluation state.")
public record ExamQuestionSlimDto(

        @Schema(
                description = "Unique identifier of the exam question.",
                example = "3001"
        )
        Long id,

        @Schema(
                description = "The original question associated with this exam question.",
                implementation = QuestionSlimDto.class
        )
        QuestionSlimDto question,

        @Schema(
                description = "Free text answer provided by the user (if applicable).",
                example = "The capital of France is Paris."
        )
        String freeTextAnswer,

        @Schema(
                description = "Whether the user's answer(s) were evaluated as correct.",
                example = "false"
        )
        Boolean isCorrect,

        @Schema(
                description = "List of answers selected by the user for this question.",
                implementation = AnswerSlimDto.class,
                type = SchemaType.ARRAY
        )
        List<AnswerSlimDto> selectedAnswers
) {}