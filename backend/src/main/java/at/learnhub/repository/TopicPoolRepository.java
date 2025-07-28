package at.learnhub.repository;

import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.dto.simple.SubjectDto;
import at.learnhub.dto.simple.TopicPoolDto;
import at.learnhub.mapper.QuestionMapper;
import at.learnhub.mapper.SubjectMapper;
import at.learnhub.mapper.TopicPoolMapper;
import at.learnhub.model.Question;
import at.learnhub.model.TopicPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

/**
 * Repository class for accessing {@link TopicPool} data from the database.
 * Handles fetching and converting TopicPool entities to DTOs.
 */
@ApplicationScoped
public class TopicPoolRepository {
    @Inject
    EntityManager em;

    /**
     * Retrieves all {@link TopicPool} entities from the database and maps them to {@link TopicPoolDto} objects.
     *
     * @return a list of all topic pools as DTOs, including their relations
     */
    public List<TopicPoolDto> findAll() {
        return em.createQuery("select t from TopicPool t", TopicPool.class).getResultList()
                .stream().map(TopicPoolMapper::toDto).toList();
    }

    public TopicPoolDto getTopicPoolDtoById(Long id) {
        TopicPool topicPool = getTopicPoolById(id);
        return TopicPoolMapper.toDto(topicPool);
    }

    public TopicPool getTopicPoolById(Long id) {
        TopicPool topicPool = em.find(TopicPool.class, id);
        if (topicPool == null) {
            throw new EntityNotFoundException("Topic pool with id " + id + " not found.");
        }
        return topicPool;
    }
}
