package at.learnhub.mapper;

import at.learnhub.dto.simple.ExamDto;
import at.learnhub.dto.simple.ExamQuestionSlimDto;
import at.learnhub.model.Exam;
import at.learnhub.model.ExamQuestion;

public class ExamQuestionMapper {

    public static ExamQuestionSlimDto toSlimDto(ExamQuestion eq) {
        return new ExamQuestionSlimDto(
                eq.getId(),
                QuestionMapper.toSlimDto(eq.getEntry().getQuestion()),
                eq.getFreeTextAnswer(), eq.getCorrect(),
                eq.getSelectedAnswers().stream().map(AnswerMapper::toSlimDto).toList()
        );
    }
}
