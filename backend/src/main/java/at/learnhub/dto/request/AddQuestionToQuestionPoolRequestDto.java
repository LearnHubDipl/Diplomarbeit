package at.learnhub.dto.request;

import at.learnhub.dto.simple.AnswerSlimDto;
import at.learnhub.model.MediaFile;
import at.learnhub.model.QuestionType;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTO for requesting to add questions to a certain question pool given id of user and questions")
public record AddQuestionToQuestionPoolRequestDto(

        @Schema(
                description = "The ID of the user owning the question pool",
                example = "1"
        )
        Long userId,

        @Schema(
                description = "The IDs of the questions that should be added to the pool",
                example = "[2, 4, 5]",
                type = SchemaType.ARRAY
        )
        List<Long> questionIds

) {}
