package at.learnhub.repository;

import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.mapper.QuestionMapper;
import at.learnhub.model.Question;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.stream.Collectors;

public class QuestionRepository {
    @Inject
    EntityManager em;

    public List<QuestionDto> findAll() {
        return em.createQuery("SELECT q FROM Question q", Question.class)
                .getResultList()
                .stream()
                .map(QuestionMapper::toDto)
                .collect(Collectors.toList());
    }
}
