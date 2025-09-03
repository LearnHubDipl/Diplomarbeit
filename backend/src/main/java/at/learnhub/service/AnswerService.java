package at.learnhub.service;

import at.learnhub.dto.request.CheckAnswersRequestDto;
import at.learnhub.dto.response.CheckAnswersResponseDto;
import at.learnhub.model.Question;
import at.learnhub.repository.AnswerRepository;
import at.learnhub.repository.QuestionRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.BadRequestException;

import java.util.HashSet;
import java.util.List;

@ApplicationScoped
public class AnswerService {
    @Inject
    QuestionRepository questionRepository;
    @Inject
    AnswerRepository answerRepository;

    public CheckAnswersResponseDto checkAnswers(CheckAnswersRequestDto request) {
        if (request.questionId() == null) {
            throw new BadRequestException("Missing question ID");
        }

        Question question = questionRepository.getQuestionById(request.questionId());

        return switch (question.getType()) {
            case MULTIPLE_CHOICE -> checkMultipleChoiceAnswer(question, request);
            case FREETEXT -> checkFreeTextAnswer(question, request);
        };
    }


    private CheckAnswersResponseDto checkMultipleChoiceAnswer(Question question, CheckAnswersRequestDto request) {
        boolean isCorrect = false;
        List<Long> correctIds = answerRepository.getCorrectAnswersIdsForQuestion(question.getId());

        if (request.selectedAnswerIds() == null || request.selectedAnswerIds().isEmpty()) {
            // no answers provided
            isCorrect = false;
        } else {
            // check if provided answers are correct
            isCorrect = new HashSet<>(correctIds).equals(new HashSet<>(request.selectedAnswerIds()));
        }

        return new CheckAnswersResponseDto(
                question.getId(),
                isCorrect,
                correctIds,
                List.of(),
                question.getExplanation()
        );
    }

    private CheckAnswersResponseDto checkFreeTextAnswer(Question question, CheckAnswersRequestDto request) {
        String userAnswer = request.freeTextAnswer();
        List<String> correctAnswers = answerRepository.getCorrectAnswersForFreeTextQuestion(question.getId());
        boolean isCorrect;

        if (userAnswer == null || userAnswer.isBlank()) {
            // no answer provided
            isCorrect = false;
        } else {
            isCorrect = correctAnswers.stream()
                    .anyMatch(correct -> correct.equalsIgnoreCase(userAnswer.trim()));
        }

        return new CheckAnswersResponseDto(
                question.getId(),
                isCorrect,
                List.of(),
                correctAnswers,
                question.getExplanation());
    }
}
