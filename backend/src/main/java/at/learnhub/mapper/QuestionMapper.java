package at.learnhub.mapper;

import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.dto.simple.QuestionSlimDto;
import at.learnhub.model.Question;

public class QuestionMapper {

    public static QuestionDto toDto(Question question) {
    }

    public static QuestionSlimDto toSlimDto(Question question) {
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