package at.learnhub.repository;

import at.learnhub.dto.request.AddQuestionToQuestionPoolRequestDto;
import at.learnhub.dto.simple.QuestionPoolDto;
import at.learnhub.dto.simple.QuestionPoolEntrySlimDto;
import at.learnhub.mapper.QuestionPoolEntryMapper;
import at.learnhub.mapper.QuestionPoolMapper;
import at.learnhub.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.NotFoundException;

import java.util.List;
import java.util.Objects;

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

    public List<QuestionPoolEntrySlimDto> findByTopicPool(Long userId, Long topicPoolId) {
        QuestionPool pool = findEntityByUserId(userId);
        return pool.getEntries().stream()
                .filter(e -> e.getQuestion().getTopicPool().getId().equals(topicPoolId))
                .map(QuestionPoolEntryMapper::toSlimDto).toList();
    }

    public List<TopicPool> findTopicPoolsByUserId(Long userId) {
        QuestionPool pool = findEntityByUserId(userId);

        return pool.getEntries().stream()
                .map(e -> e.getQuestion().getTopicPool())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
