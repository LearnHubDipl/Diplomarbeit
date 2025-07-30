package at.learnhub.repository;

import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.mapper.QuestionMapper;
import at.learnhub.model.Question;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository class for managing {@link Question} entities in the database.
 */
@ApplicationScoped
public class QuestionRepository {
    @Inject
    EntityManager em;

    /**
     * Retrieves all questions from the database and maps them to Dtos.
     * @return
     */
    public List<QuestionDto> findAll() {
        return em.createQuery("SELECT q FROM Question q", Question.class)
                .getResultList()
                .stream()
                .map(QuestionMapper::toDto)
                .collect(Collectors.toList());
    }
}
