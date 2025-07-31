package at.learnhub.repository;

import at.learnhub.dto.request.AddQuestionToQuestionPoolRequestDto;
import at.learnhub.dto.simple.QuestionPoolDto;
import at.learnhub.mapper.QuestionPoolMapper;
import at.learnhub.model.QuestionPool;
import at.learnhub.model.QuestionPoolEntry;
import at.learnhub.model.StreakTracking;
import at.learnhub.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@ApplicationScoped
public class QuestionPoolRepository {
    @Inject
    EntityManager em;

    public QuestionPoolDto findByUserId(Long userId) {
        return QuestionPoolMapper.toDto(findEntityByUserId(userId));
    }

    public QuestionPool findEntityByUserId(Long userId) {
        QuestionPool pool = em.createQuery(
                "SELECT q from QuestionPool q " +
                        "WHERE q.user.id = :id", QuestionPool.class)
                .setParameter("id", userId)
                .getSingleResult();
        if (pool == null) throw new NotFoundException("Question pool of user with id " + userId + " not found");
        return pool;
    }
}
