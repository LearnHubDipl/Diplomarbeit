package at.learnhub.mapper;

import at.learnhub.dto.simple.ExamDto;
import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.dto.simple.QuestionSlimDto;
import at.learnhub.dto.simple.SolutionSlimDto;
import at.learnhub.model.Exam;
import at.learnhub.model.Question;
import at.learnhub.model.QuestionType;

import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class ExamMapper {

    public static ExamDto toDto(Exam exam) {
        return new ExamDto(
                exam.getId(), exam.getTimeLimit(),
                exam.getStartedAt(), exam.getFinishedAt(),
                exam.getQuestionCount(), exam.getScore(),
                UserMapper.toSlimDto(exam.getUser()),
                exam.getTopicPools().stream().map(TopicPoolMapper::toSlimDto).toList(),
                exam.getQuestions().stream().map(ExamQuestionMapper::toSlimDto).toList()
        );
    }
}
