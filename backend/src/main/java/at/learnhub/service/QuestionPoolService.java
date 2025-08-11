package at.learnhub.service;

import at.learnhub.dto.request.AddQuestionToQuestionPoolRequestDto;
import at.learnhub.dto.simple.QuestionPoolDto;
import at.learnhub.model.*;
import at.learnhub.repository.QuestionPoolRepository;
import at.learnhub.repository.QuestionRepository;
import at.learnhub.repository.StreakTrackingRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.time.LocalDate;

@ApplicationScoped
public class QuestionPoolService {
    @Inject
    EntityManager em;
    @Inject
    QuestionPoolRepository questionPoolRepository;
    @Inject
    QuestionRepository questionRepository;


    @Transactional
    public QuestionPoolDto addQuestionsToPool(AddQuestionToQuestionPoolRequestDto request) {
        QuestionPool pool = questionPoolRepository.findEntityByUserId(request.userId());
        for(Long currentQuestionId: request.questionIds()) {
            Question question = questionRepository.getQuestionById(currentQuestionId);

            // Check if the question is already in the pool
            boolean alreadyExists = pool.getEntries().stream()
                    .anyMatch(entry -> entry.getQuestion().getId().equals(question.getId()));

            if(!alreadyExists) {
                QuestionPoolEntry entry = new QuestionPoolEntry();
                entry.setQuestion(question);
                entry.setQuestionPool(pool);
                entry.setCorrectCount(0);
                entry.setLastAnsweredCorrectly(false);

                pool.getEntries().add(entry);
                question.getEntries().add(entry);

                em.persist(entry);
            }
        }
        return questionPoolRepository.findByUserId(request.userId());
    }
}
