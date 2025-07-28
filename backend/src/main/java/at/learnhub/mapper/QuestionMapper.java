package at.learnhub.mapper;

import at.learnhub.dto.simple.AnswerSlimDto;
import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.dto.simple.QuestionSlimDto;
import at.learnhub.model.Question;
import at.learnhub.model.QuestionType;
import at.learnhub.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestionMapper {
    public static QuestionDto toDto(Question question) {
        List<AnswerSlimDto> answers = new ArrayList<>();
        if (question.getType() != QuestionType.FREETEXT) {
            answers = question.getAnswers().stream()
                    .map(AnswerMapper::toSlimDto)
                    .toList();
        }

        return new QuestionDto(
                question.getId(),
                question.getText(),
                question.getExplanation(),
                question.getMedia(),
                question.getType(),
                question.getDifficulty(),
                question.getPublic(),
                TopicPoolMapper.toSlimDto(question.getTopicPool()),
                UserMapper.toSlimDto(question.getUser()),
                answers
        );
    }

    public static QuestionSlimDto toSlimDto(Question question) {
        return new QuestionSlimDto(
                question.getId(),
                question.getText(),
                question.getExplanation(),
                question.getMedia(),
                question.getType(),
                question.getDifficulty(),
                question.getPublic()
        );
    }

    public static Question toEntity(QuestionDto questionDto) {
        Question question = new Question();
        question.setId(question.getId());
        question.setText(question.getText());
        question.setExplanation(question.getExplanation());
        question.setMedia(question.getMedia());
        question.setType(question.getType());
        question.setDifficulty(question.getDifficulty());
        question.setPublic(question.getPublic());
        return question;
    }
}