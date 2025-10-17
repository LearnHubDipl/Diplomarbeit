package at.learnhub.repository;
import at.learnhub.model.QuestionPoolEntry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class QuestionPoolEntryRepository {
    @Inject
    EntityManager em;

    public QuestionPoolEntry findById(Long id) {
        return em.find(QuestionPoolEntry.class, id);
    }

    public QuestionPoolEntry save(QuestionPoolEntry entry) {
        if (entry.getId() == null) {
            em.persist(entry);
            return entry;
        } else {
            return em.merge(entry);
        }
    }

    public void delete(Long id) {
        QuestionPoolEntry entry = findById(id);
        if (entry != null) {
            em.remove(entry);
        }
    }

    public QuestionPoolEntry findByQuestionIdAndUserId(Long questionId, Long userId) {
        String jpql = """
            SELECT e 
            FROM QuestionPoolEntry e 
            WHERE e.question.id = :questionId 
              AND e.questionPool.user.id = :userId
        """;

        return em.createQuery(jpql, QuestionPoolEntry.class)
                .setParameter("questionId", questionId)
                .setParameter("userId", userId)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public List<QuestionPoolEntry> findAllByUserAndOptionalTopicPool(Long userId, Long topicPoolId) {
        String jpql = "SELECT e FROM QuestionPoolEntry e WHERE e.questionPool.user.id = :userId";
        if (topicPoolId != null) {
            jpql += " AND e.question.topicPool.id = :topicPoolId";
        }

        var query = em.createQuery(jpql, QuestionPoolEntry.class)
                .setParameter("userId", userId);

        if (topicPoolId != null) {
            query.setParameter("topicPoolId", topicPoolId);
        }

        return query.getResultList();
    }

}
