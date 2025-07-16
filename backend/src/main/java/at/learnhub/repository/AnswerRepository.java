package at.learnhub.repository;

import at.learnhub.dto.request.CheckAnswersRequestDto;
import at.learnhub.dto.response.CheckAnswersResponseDto;
import at.learnhub.dto.simple.AnswerDto;
import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.mapper.AnswerMapper;
import at.learnhub.mapper.QuestionMapper;
import at.learnhub.model.Answer;
import at.learnhub.model.Question;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AnswerRepository {
    @Inject
    EntityManager em;

    public List<AnswerDto> findAll() {
        return em.createQuery("SELECT a FROM Answer a", Answer.class)
                .getResultList()
                .stream()
                .map(AnswerMapper::toDto)
                .collect(Collectors.toList());
    }


    private Answer getAnswerById(Long id) {
        Answer answer = em.find(Answer.class, id);
        if (answer == null) {
            throw new EntityNotFoundException("Answer with id " + id + " not found.");
        }
        return answer;
    }

    public List<Long> getCorrectAnswersIdsForQuestion(Long questionId) {
        return em.createQuery("SELECT a.id FROM Answer a " +
                        "WHERE a.question.id = :questionId AND a.isCorrect = true", Long.class)
                .setParameter("questionId", questionId)
                .getResultList();
    }
}
