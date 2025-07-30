package at.learnhub.mapper;

import at.learnhub.dto.simple.AnswerSlimDto;
import at.learnhub.model.Answer;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 *  Utility class for converting between {@link Answer} entities and their DTO representations.
 */
public class AnswerMapper {

    /**
     * Converts an Asnwer entity to a simplified AnswerSlimDto
     * @param answer the answer entity
     * @return the corresponding AnswerSlimDto
     */
    public static AnswerSlimDto toSlimDto(Answer answer) {
        return new AnswerSlimDto(
                answer.getId(),
                answer.getText(),
                answer.getCorrect()
        );
    }
}