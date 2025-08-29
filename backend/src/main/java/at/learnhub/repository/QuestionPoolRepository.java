package at.learnhub.repository;

import at.learnhub.dto.request.AddQuestionToQuestionPoolRequestDto;
import at.learnhub.dto.simple.*;
import at.learnhub.mapper.*;
import at.learnhub.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
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
        QuestionPool pool = em.createQuery(
                "SELECT q from QuestionPool q " +
                        "WHERE q.user.id = :id", QuestionPool.class)
                .setParameter("id", userId)
                .getSingleResult();
        if (pool == null) throw new EntityNotFoundException("Question pool of user with id " + userId + " not found");
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

    public List<SubjectDto> findSubjectsAndTopicPoolsByUserId(Long userId) {
        QuestionPool pool = findEntityByUserId(userId);

        List<TopicPool> topicPools = findTopicPoolsByUserId(userId);

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

}
