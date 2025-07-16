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
        if (request.selectedAnswerIds() == null || request.selectedAnswerIds().isEmpty()) {
            throw new BadRequestException("No answers selected");
        }

        Question question = questionRepository.getQuestionById(request.questionId());

        List<Long> correctAnswerIds = answerRepository.getCorrectAnswersIdsForQuestion(request.questionId());

        boolean isCorrect = new HashSet<>(request.selectedAnswerIds())
                .equals(new HashSet<>(correctAnswerIds));

        return new CheckAnswersResponseDto(
                question.getId(),
                isCorrect,
                correctAnswerIds,
                question.getExplanation()
        );
    }
}
