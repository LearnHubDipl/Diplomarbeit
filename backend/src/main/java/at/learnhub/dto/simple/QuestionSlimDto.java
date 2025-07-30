package at.learnhub.dto.simple;

import at.learnhub.model.MediaFile;
import at.learnhub.model.QuestionType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "QuestionSlimDto", description = "Slimmed info about a question")
public record QuestionSlimDto(
        @Schema(
                description = "Unique identifier of the question",
                example = "2"
        )
        Long id,

        @Schema(
                description = "Text content of the question",
                example = "What is the capital of France"
        )
        String text,

        @Schema(
                description = "The explanation of the question",
                example = "Paris is the capital"
        )
        String explanation,

        @Schema(
                description = "Media file"
        )
        MediaFile media,

        @Schema(
                description = "Type of the question"
        )
        QuestionType type,

        @Schema(
                description = "Difficulty level: 1=easy, 2=medium, 3=hard",
                example = "2"
        )
        Integer difficulty,

        @Schema(
                description = "Shows if the question is public",
                example = "true"
        )
        Boolean isPublic
) {
}