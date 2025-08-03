package at.learnhub.dto.request;

import at.learnhub.dto.simple.AnswerSlimDto;
import at.learnhub.dto.simple.SolutionSlimDto;
import at.learnhub.dto.simple.TopicPoolSlimDto;
import at.learnhub.dto.simple.UserSlimDto;
import at.learnhub.model.MediaFile;
import at.learnhub.model.QuestionType;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTO representing a quiz question with core details for API communication.")
public record QuestionCreationRequestDto(

        @Schema(
                description = "The main text or body of the question presented to the user.",
                example = "What is the capital of France?"
        )
        String text,

        @Schema(
                description = "Detailed explanation behind the correct answer(s). Useful for feedback.",
                example = "Paris is the capital and largest city of France."
        )
        String explanation,

        /**
        @Schema(
                description = "Optional media file associated with this question (e.g., image, diagram).",
                implementation = MediaFile.class
        )
        MediaFile media,

         **/
        //todo: implement media file

        @Schema(
                description = "Numerical code representing the question type (e.g., multiple choice, freetext).",
                example = "1",
                implementation = QuestionType.class
        )
        QuestionType type,

        @Schema(
                description = "Difficulty level of the question, e.g., 1 (easy) to 3 (hard).",
                example = "3"
        )
        Integer difficulty,

        @Schema(
                description = "Whether this question is publicly available to all users.",
                example = "true"
        )
        Boolean isPublic,

        @Schema(
                description = "ID of user who created the question",
                example = "1"
        )
        Long userId,

        @Schema(
                description = "ID of the topic pool this question belongs to. Used to categorize questions.",
                example = "1"
        )
        Long topicPoolId,

        @Schema(
                description = "List of possible answers for this question.",
                implementation = AnswerCreationRequestDto.class,
                type = SchemaType.ARRAY
        )
        List<AnswerCreationRequestDto> answers
) {}