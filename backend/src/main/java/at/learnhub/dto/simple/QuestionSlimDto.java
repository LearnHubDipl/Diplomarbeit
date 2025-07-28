package at.learnhub.dto.simple;

import at.learnhub.model.MediaFile;
import at.learnhub.model.QuestionType;

public record QuestionSlimDto(
        Long id,
        String text,
        String explanation,
        MediaFile media,
        QuestionType type,
        Integer difficulty,
        Boolean isPublic
) {
}