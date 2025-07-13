package at.learnhub.dto.simple;

import at.learnhub.model.MediaFile;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record QuestionSlimDto(
        @Schema(
        )
        Long id,

        @Schema(
        )
        String text,

        @Schema(
        )
        String explanation,

        @Schema(
        )
        MediaFile media,

        @Schema(
        )

        @Schema(
        )
        Integer difficulty,

        @Schema(
                example = "true"
        )
        Boolean isPublic