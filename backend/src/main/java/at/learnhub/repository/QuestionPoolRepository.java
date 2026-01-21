package at.learnhub.repository;

import at.learnhub.dto.request.AddQuestionToQuestionPoolRequestDto;
import at.learnhub.dto.simple.*;
import at.learnhub.mapper.*;
import at.learnhub.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.ws.rs.NotFoundException;

import java.util.ArrayList;
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
        try {
            return em.createQuery(
                            "SELECT q from QuestionPool q WHERE q.user.id = :id", QuestionPool.class)
                    .setParameter("id", userId)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new EntityNotFoundException("Question pool of user with id " + userId + " not found");
        }
    }

    public List<QuestionPoolEntrySlimDto> findByTopicPool(Long userId, Long topicPoolId) {
        QuestionPool pool = findEntityByUserId(userId);
        return pool.getEntries().stream()
                .filter(e -> e.getQuestion().getTopicPool().getId().equals(topicPoolId))
                .map(QuestionPoolEntryMapper::toSlimDto).toList();
    }

    public List<TopicPoolSlimDto> findTopicPoolsByUserId(Long userId) {
        QuestionPool pool = findEntityByUserId(userId);

        return pool.getEntries().stream()
                .map(e -> e.getQuestion().getTopicPool())
                .filter(Objects::nonNull)
                .distinct()
                .map(TopicPoolMapper::toSlimDto)
                .toList();
    }

    public List<SubjectDto> findSubjectsAndTopicPoolsByUserId(Long userId) {
        QuestionPool pool = findEntityByUserId(userId);

        List<TopicPool> topicPools = pool.getEntries().stream()
                .map(e -> e.getQuestion().getTopicPool())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<Subject> subjects = topicPools.stream()
                .map(tp -> tp.getSubject())
                .distinct()
                .toList();

        List<SubjectDto> result = new ArrayList<>();
        for (Subject subject : subjects) {
            List<TopicPool> pools = topicPools.stream()
                    .filter(tp -> tp.getSubject().equals(subject))
                    .toList();

            SubjectDto dto = new SubjectDto(
                    subject.getId(),
                    subject.getName(),
                    subject.getDescription(),
                    MediaFileMapper.toSlimDto(subject.getImg()),
                    pools.stream().map(TopicPoolMapper::toSlimDto).toList()
            );

            result.add(dto);
        }

        return result;
    }

    public QuestionPoolDto removeQuestions(Long userId, List<Long> questionIds) {
        List<QuestionPoolEntry> entriesToRemove = em.createQuery(
                        "SELECT e FROM QuestionPoolEntry e WHERE e.questionPool.user.id = :userId AND e.question.id IN :questionIds",
                        QuestionPoolEntry.class
                )
                .setParameter("userId", userId)
                .setParameter("questionIds", questionIds)
                .getResultList();

        for (QuestionPoolEntry entry : entriesToRemove) {
            em.remove(entry);
        }

        return findByUserId(userId);
    }


    public List<Long> findQuestionIdsByTopicPools(Long userId, List<Long> topicPoolIds) {
        return em.createQuery(
                        "SELECT q.id FROM Question q " +
                                "JOIN QuestionPoolEntry e ON e.question.id = q.id " +
                                "WHERE e.questionPool.user.id = :userId " +
                                "AND q.topicPool.id IN :topicPoolIds " +
                                "AND (" +
                                "  e.answeredAt IS NULL OR " +
                                "  e.lastAnsweredCorrectly IS NULL OR " +
                                "  e.lastAnsweredCorrectly = false OR " +
                                "  (e.correctCount = 1 AND e.answeredAt <= CAST(CURRENT_TIMESTAMP AS LocalDateTime) - 1 DAY) OR " +
                                "  (e.correctCount = 2 AND e.answeredAt <= CAST(CURRENT_TIMESTAMP AS LocalDateTime) - 7 DAY)" +
                                ")", Long.class)
                .setParameter("userId", userId)
                .setParameter("topicPoolIds", topicPoolIds)
                .getResultList();
    }

    public List<Long> findAllQuestionIdsByUserId(Long userId) {
        return em.createQuery(
                        "SELECT e.question.id FROM QuestionPoolEntry e " +
                                "WHERE e.questionPool.user.id = :userId " +
                                "AND (" +
                                "  e.answeredAt IS NULL OR " +
                                "  e.lastAnsweredCorrectly IS NULL OR " +
                                "  e.lastAnsweredCorrectly = false OR " +
                                "  (e.correctCount = 1 AND e.answeredAt <= CAST(CURRENT_TIMESTAMP AS LocalDateTime) - 1 DAY) OR " +
                                "  (e.correctCount = 2 AND e.answeredAt <= CAST(CURRENT_TIMESTAMP AS LocalDateTime) - 7 DAY) OR " +
                                "  (e.correctCount >= 3 AND e.answeredAt <= CAST(CURRENT_TIMESTAMP AS LocalDateTime) - 30 DAY)" +
                                ")", Long.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
