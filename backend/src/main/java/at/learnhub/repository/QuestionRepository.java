package at.learnhub.repository;

import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.model.Question;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class QuestionRepository {
    @Inject
    EntityManager em;

    public List<QuestionDto> findAll() {
        return em.createQuery("SELECT q FROM Question q", Question.class)
                .getResultList()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    public QuestionDto getQuestionDtoById(Long id) {
        Question question = em.find(Question.class, id);
        if (question == null) {
            throw new EntityNotFoundException("Question with id " + id + " not found.");
        }
        return convertToDto(question);
    }
}
