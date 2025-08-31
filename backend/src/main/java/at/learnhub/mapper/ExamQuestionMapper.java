package at.learnhub.mapper;

import at.learnhub.dto.simple.ExamDto;
import at.learnhub.dto.simple.ExamQuestionSlimDto;
import at.learnhub.model.*;

import java.util.List;

public class ExamQuestionMapper {

    public static ExamQuestionSlimDto toSlimDto(ExamQuestion eq) {
        Question q = eq.getEntry().getQuestion();

        List<Long> correctAnswerIds = null;
        List<String> correctFreeTextAnswers = null;

        if (q.getType() == QuestionType.MULTIPLE_CHOICE) {
            correctAnswerIds = q.getAnswers().stream()
                    .filter(Answer::getCorrect)
                    .map(Answer::getId)
                    .toList();
        } else if (q.getType() == QuestionType.FREETEXT) {
            correctFreeTextAnswers = q.getAnswers().stream()
                    .filter(Answer::getCorrect)
                    .map(Answer::getText)
                    .toList();
        }

        return new ExamQuestionSlimDto(
                eq.getId(),
                QuestionMapper.toDto(eq.getEntry().getQuestion()),
                eq.getFreeTextAnswer(), eq.getCorrect(),
                eq.getSelectedAnswers().stream().map(AnswerMapper::toSlimDto).toList(),
                correctAnswerIds, correctFreeTextAnswers
        );
    }
}
