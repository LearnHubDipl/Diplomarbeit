package at.learnhub.dto.simple;

import at.learnhub.model.MediaFile;
import at.learnhub.model.QuestionType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

public record QuestionDto(
        Long id,
        String text,
        String explanation,
        MediaFile media,
        QuestionType type,
        Integer difficulty,
        Boolean isPublic,
        TopicPoolSlimDto topicPool,
        UserSlimDto user,
        List<AnswerSlimDto> answers
        //todo: solutionsslimdto
) {
}