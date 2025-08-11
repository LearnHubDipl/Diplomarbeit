package at.learnhub.repository;

import at.learnhub.dto.simple.TopicContentSlimDto;
import at.learnhub.mapper.TopicContentMapper;
import at.learnhub.model.TopicContent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class TopicContentRepository {

    @Inject
    EntityManager em;

    public List<TopicContentSlimDto> findApprovedBySubject(Long subjectId) {
        return em.createQuery("""
                SELECT tc FROM TopicContent tc
                WHERE tc.isApproved = true AND tc.topicPool.subject.id = :sid
                ORDER BY tc.date DESC
                """, TopicContent.class)
                .setParameter("sid", subjectId)
                .getResultList()
                .stream()
                .map(TopicContentMapper::toSlimDto)
                .collect(Collectors.toList());
    }

    public List<TopicContentSlimDto> findApprovedBySubjectAndTopic(Long subjectId, Long topicPoolId) {
        return em.createQuery("""
                SELECT tc FROM TopicContent tc
                WHERE tc.isApproved = true 
                  AND tc.topicPool.subject.id = :sid 
                  AND tc.topicPool.id = :tpid
                ORDER BY tc.date DESC
                """, TopicContent.class)
                .setParameter("sid", subjectId)
                .setParameter("tpid", topicPoolId)
                .getResultList()
                .stream()
                .map(TopicContentMapper::toSlimDto)
                .collect(Collectors.toList());
    }

    public TopicContent getById(Long id) {
        TopicContent tc = em.find(TopicContent.class, id);
        if (tc == null) {
            throw new EntityNotFoundException("TopicContent with id " + id + " not found.");
        }
        return tc;
    }

    @Transactional
    public TopicContent create(TopicContent tc) {
        em.persist(tc);
        return tc;
    }

    @Transactional
    public TopicContent update(TopicContent tc) {
        return em.merge(tc);
    }

    @Transactional
    public void delete(Long id) {
        TopicContent tc = getById(id);
        em.remove(tc);
    }
}
