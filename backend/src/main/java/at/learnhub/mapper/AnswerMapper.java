package at.learnhub.mapper;

import at.learnhub.dto.simple.AnswerDto;
import at.learnhub.dto.simple.AnswerSlimDto;
import at.learnhub.dto.simple.AnswerSlimDto;
import at.learnhub.model.Answer;
import at.learnhub.model.Question;

/**
 * Utility class responsible for converting between {@link Answer} entities and their DTO representations.
 * <p>
 * This includes:
 * <ul>
 *     <li>{@link AnswerDto} — full DTO including answers and solutions</li>
 *     <li>{@link AnswerSlimDto} — simplified DTO excluding relations</li>
 * </ul>
 *
 * This class is stateless and should not be instantiated.
 */
public class AnswerMapper {

    /**
     * Maps a Answer entity to a full AnswerDto including question
     *
     * @param answer the Answer entity
     * @return the corresponding AnswerDto
     */
    public static AnswerDto toDto(Answer answer) {
        return new AnswerDto(
                answer.getId(),
                answer.getText(),
                QuestionMapper.toSlimDto(answer.getQuestion())
        );
    }

    /**
     * Maps an Answer entity to a slim AnswerSlimDto, excluding relations.
     *
     * @param answer the Answer entity
     * @return the slim AnswerSlimDto
     */
    public static AnswerSlimDto toSlimDto(Answer answer) {
        return new AnswerSlimDto(
                answer.getId(),
                answer.getText()
        );
    }

    /**
     * Returns a new Answer created from the information contained in the provided DTO.
     *
     * @param answerDto the Answer DTO
     * @return the newly created Answer
     */
    public static Answer toEntity(AnswerDto answerDto) {
        Answer answer = new Answer();
        answer.setId(answer.getId());
        answer.setText(answer.getText());
        answer.setCorrect(answer.getCorrect());
        answer.setQuestion(answer.getQuestion());
        return answer;
    }

}
