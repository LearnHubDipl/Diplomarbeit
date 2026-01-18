package at.learnhub.mapper;

import at.learnhub.dto.simple.*;
import at.learnhub.model.Exam;
import at.learnhub.model.Question;
import at.learnhub.model.QuestionType;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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


    public static ExamHistoryDto toHistoryDto(Exam entity) {
        List<String> subjects = entity.getTopicPools().stream()
                .map(tp -> tp.getSubject().getName())
                .distinct().toList();

        return new ExamHistoryDto(
                entity.getId(),
                entity.getScore(),
                entity.getQuestionCount(),
                entity.getStartedAt(),
                entity.getFinishedAt(),
                subjects
        );
    }
}
