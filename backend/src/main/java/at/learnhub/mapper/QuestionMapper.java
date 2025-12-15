package at.learnhub.mapper;

import at.learnhub.dto.simple.*;
import at.learnhub.model.Question;
import at.learnhub.model.QuestionType;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import at.learnhub.mapper.TopicPoolMapper;

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
     * @return the corresponding QuestionDto
     */
    public static QuestionDto toDto(Question question) {
        if (question == null) {
            return null;
        }

        // Handle null answers list
        List<AnswerSlimDto> answerDtos = question.getAnswers() == null
                ? List.of()
                : question.getAnswers().stream()
                .map(AnswerMapper::toSlimDto)
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    if (question.getType() != QuestionType.FREETEXT) {
                        Collections.shuffle(list);
                    }
                    return list;
                }));

        // Handle null solutions list - THIS IS THE FIX!
        List<SolutionSlimDto> solutionDtos = question.getSolutions() == null
                ? List.of()
                : question.getSolutions().stream()
                .map(SolutionMapper::toSlimDto)
                .sorted(Comparator.comparingLong(SolutionSlimDto::upVotes).reversed())
                .toList();

        return new QuestionDto(
                question.getId(),
                question.getText(),
                question.getExplanation(),
                question.getMedia(),
                question.getType(),
                question.getDifficulty(),
                question.getPublic(),
                UserMapper.toSlimDto(question.getUser()),
                TopicPoolMapper.toSlimDto(question.getTopicPool()),
                answerDtos,
                solutionDtos
        );
    }


    /**
     * Maps a Question entity to a slim QuestionSlimDto, excluding relations.
     *
     * @param question the Question entity
     * @return the slim QuestionSlimDto
     */
    public static QuestionSlimDto toSlimDto(Question question) {
        if (question == null) {
            return null;
        }

        List<AnswerSlimDto> answerDtos = question.getAnswers() == null
                ? List.of()
                : question.getAnswers().stream()
                .map(AnswerMapper::toSlimDto)
                .toList();

        return new QuestionSlimDto(
                question.getId(),
                question.getText(),
                question.getExplanation(),
                question.getMedia(),
                question.getType(),
                question.getDifficulty(),
                question.getPublic(),
                answerDtos
        );
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
