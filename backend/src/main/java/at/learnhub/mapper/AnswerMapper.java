package at.learnhub.mapper;

import at.learnhub.dto.simple.AnswerSlimDto;
import at.learnhub.model.Answer;

public class AnswerMapper {
    public static AnswerSlimDto toSlimDto(Answer answer) {
        return new AnswerSlimDto(
                answer.getId(),
                answer.getText(),
                answer.getCorrect()
        );
    }
}