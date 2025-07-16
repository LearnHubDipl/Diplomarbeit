package at.learnhub.mapper;

import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.dto.simple.QuestionSlimDto;
import at.learnhub.model.Question;
import at.learnhub.model.QuestionType;

/**
 * Utility class responsible for converting between {@link Question} entities and their DTO representations.
 * <p>
 * This includes:
 * <ul>
 *     <li>{@link QuestionDto} — full DTO including answers and solutions</li>
 *     <li>{@link QuestionSlimDto} — simplified DTO excluding relations</li>
 * </ul>
 *
 * This class is stateless and should not be instantiated.
 */
public class QuestionMapper {

    /**
     * Maps a Question entity to a full QuestionDto including solutions and answers
     *
     * @param question the Question entity
     * @return the corresponding SubjectDto
     */
    public static QuestionDto toDto(Question question) {
        return new QuestionDto(question.getId(), question.getText(), question.getExplanation(),
                question.getMedia(), question.getType(), question.getDifficulty(),
                question.getPublic(), TopicPoolMapper.toSlimDto(question.getTopicPool()),
                question.getType() == QuestionType.FREETEXT ? null :
                question.getAnswers().stream().map(AnswerMapper::toSlimDto).toList(),
                question.getSolutions().stream().map(SolutionMapper::toSlimDto).toList());
    }

    /**
     * Maps a Question entity to a slim QuestionSlimDto, excluding relations.
     *
     * @param question the Subject entity
     * @return the slim SubjectSlimDto
     */
    public static QuestionSlimDto toSlimDto(Question question) {
        return new QuestionSlimDto(question.getId(), question.getText(), question.getExplanation(),
                question.getMedia(), question.getType(), question.getDifficulty(), question.getPublic());
    }

    /**
     * Returns a new Question created from the information contained in the provided DTO.
     *
     * @param questionDto the Question DTO
     * @return the newly created Question
     */
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
