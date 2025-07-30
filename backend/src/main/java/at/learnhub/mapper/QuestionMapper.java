package at.learnhub.mapper;

import at.learnhub.dto.simple.AnswerSlimDto;
import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.dto.simple.QuestionSlimDto;
import at.learnhub.dto.simple.TopicPoolSlimDto;
import at.learnhub.model.Question;
import at.learnhub.model.QuestionType;
import at.learnhub.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuestionMapper {

    public static QuestionDto toDto(Question question) {
        List<AnswerSlimDto> answers = new ArrayList<>();
        if(question.getType() != QuestionType.FREETEXT){
            // Nullprüfung ergänzen:
            if(question.getAnswers() != null){
                answers = question.getAnswers().stream()
                        .map(AnswerMapper::toSlimDto)
                        .toList();
            }
        }

        TopicPoolSlimDto topicPoolDto = null;
        if(question.getTopicPool() != null){
            topicPoolDto = TopicPoolMapper.toSlimDto(question.getTopicPool());
        }

        return new QuestionDto(
                question.getId(),
                question.getText(),
                question.getExplanation(),
                question.getMedia(),
                question.getType(),
                question.getDifficulty(),
                question.getPublic(),
                topicPoolDto,
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

        question.setId(questionDto.id());
        question.setText(questionDto.text());
        question.setExplanation(questionDto.explanation());
        question.setMedia(questionDto.media());
        question.setType(questionDto.type());
        question.setDifficulty(questionDto.difficulty());
        question.setPublic(questionDto.isPublic());
        return question;
    }
}