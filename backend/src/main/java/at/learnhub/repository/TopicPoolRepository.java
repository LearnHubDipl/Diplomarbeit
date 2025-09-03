package at.learnhub.repository;

import at.learnhub.dto.simple.TopicPoolSlimDto;
import at.learnhub.mapper.TopicPoolMapper;
import at.learnhub.model.TopicPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class TopicPoolRepository{

    @Inject
    EntityManager em;

    public List<TopicPoolSlimDto> findBySubjectId(Long subjectId) {
        return em.createQuery("SELECT tp FROM TopicPool tp WHERE tp.subject.id = :subjectId", TopicPool.class)
                .setParameter("subjectId", subjectId)
                .getResultList()
                .stream()
                .map(TopicPoolMapper::toSlimDto)
                .collect(Collectors.toList());
    }

    public TopicPool getTopicPoolById(Long id) {
        TopicPool tp = em.find(TopicPool.class, id);
        if (tp == null) {
            throw new EntityNotFoundException("TopicPool with id " + id + " not found.");
        }
        return tp;
    }

    public List<TopicPool> getTopicPoolListByIds(List<Long> ids) {
        List<TopicPool> topicPools = new LinkedList<>();
        for (Long id : ids) {
            topicPools.add(getTopicPoolById(id));
        }
        return topicPools;
    }

    @Transactional
    public TopicPool create(TopicPool tp) {
        em.persist(tp);
        return tp;
    }

    @Transactional
    public TopicPool update(TopicPool tp) {
        return em.merge(tp);
    }

    @Transactional
    public void delete(Long id) {
        TopicPool tp = getTopicPoolById(id);
        em.remove(tp);
    }
}
