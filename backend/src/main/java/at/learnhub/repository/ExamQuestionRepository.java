package at.learnhub.repository;

import at.learnhub.model.Exam;
import at.learnhub.model.ExamQuestion;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@ApplicationScoped
public class ExamQuestionRepository {

    @Inject
    EntityManager em;

    public ExamQuestion getEntityById(Long id) {
        ExamQuestion question = em.find(ExamQuestion.class, id);
        if (question == null) {
            throw new EntityNotFoundException("Exam Question with id " + id + " not found.");
        }
        return question;
    }
}
