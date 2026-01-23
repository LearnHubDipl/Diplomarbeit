package at.learnhub.repository;

import at.learnhub.dto.response.TopicContentDto;
import at.learnhub.mapper.TopicContentMapper;
import at.learnhub.model.MediaFile;
import at.learnhub.model.TopicContent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class TopicContentRepository {

    @Inject EntityManager em;


    public List<TopicContent> findApprovedBySubject(Long subjectId) {
        return em.createQuery("""
            select tc from TopicContent tc
            where tc.topicPool.subject.id = :sid and tc.isApproved = true
            order by tc.date desc, tc.id desc
            """, TopicContent.class)
                .setParameter("sid", subjectId)
                .getResultList();
    }

    public List<TopicContent> findApprovedBySubjectAndTopic(Long subjectId, Long topicPoolId) {
        return em.createQuery("""
            select tc from TopicContent tc
            where tc.topicPool.subject.id = :sid
              and tc.topicPool.id = :tp
              and tc.isApproved = true
            order by tc.date desc, tc.id desc
            """, TopicContent.class)
                .setParameter("sid", subjectId)
                .setParameter("tp", topicPoolId)
                .getResultList();
    }

    public List<TopicContent> findBySubjectAll(Long subjectId) {
        return em.createQuery("""
            select tc from TopicContent tc
            where tc.topicPool.subject.id = :sid
            order by tc.date desc, tc.id desc
            """, TopicContent.class)
                .setParameter("sid", subjectId)
                .getResultList();
    }

    public List<TopicContent> findBySubjectAndTopicAll(Long subjectId, Long topicPoolId) {
        return em.createQuery("""
            select tc from TopicContent tc
            where tc.topicPool.subject.id = :sid
              and tc.topicPool.id = :tp
            order by tc.date desc, tc.id desc
            """, TopicContent.class)
                .setParameter("sid", subjectId)
                .setParameter("tp", topicPoolId)
                .getResultList();
    }

    public void persist(TopicContent tc) { em.persist(tc); }

}
