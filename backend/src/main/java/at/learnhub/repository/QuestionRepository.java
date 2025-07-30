package at.learnhub.repository;

import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.mapper.QuestionMapper;
import at.learnhub.model.Question;
import at.learnhub.model.QuestionType;
import at.learnhub.model.TopicPool;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository class for managing {@link Question} entities in the database.
 */
@ApplicationScoped
public class QuestionRepository {
    @Inject
    EntityManager em;

    /**
     * Retrieves all questions from the database and maps them to Dtos.
     * @return
     */
    public List<QuestionDto> findAll() {
        return em.createQuery("SELECT q FROM Question q", Question.class)
                .getResultList()
                .stream()
                .map(QuestionMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single question by its id.
     *
     * @param id the id of the question
     * @return the mappes QuestionDto, or null if not found
     */
    public QuestionDto findById(Long id) {
        Question question = em.find(Question.class, id);
        return question != null ? QuestionMapper.toDto(question) : null;
    }

    /**
     * Retrieves all questions created by a specific user.
     *
     * @param userId the id of the user
     * @return list of question authored by the searched user
     */
    public List<QuestionDto> findByUserId(Long userId) {
        return em.createQuery("select q from Question q where q.user.id = :userId", Question.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .map(QuestionMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all public questions
     *
     * @return list of publicly visible questions
     */
    public List<QuestionDto> findAllPublicQuestions() {
        return em.createQuery("select q from Question q where q.isPublic = true", Question.class)
                .getResultList()
                .stream()
                .map(QuestionMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all questions of a given type (f.e. FREETEXT, MULTIPLE_CHOICE).
     *
     * @param type the QuestionType enum
     * @return list of questions matching the type
     */
    public List<QuestionDto> findByType(QuestionType type) {
        return em.createQuery("select q from Question q where q.type = :type", Question.class)
                .setParameter("type", type)
                .getResultList()
                .stream()
                .map(QuestionMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all questions with a specific difficulty level.
     *
     * @param difficulty this difficulty level (f.e. 1= ease, 3=hard)
     * @return list of questions with matching difficulty
     */
    public List<QuestionDto> findByDifficulty(Integer difficulty) {
        return em.createQuery("select q from Question q where q.difficulty = :difficulty", Question.class)
                .setParameter("difficulty", difficulty)
                .getResultList()
                .stream()
                .map(QuestionMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all questions belonging to a specific topic pool.
     *
     * @param topicPoolId the id of the topic pool
     * @return list of matching questions
     */
    public List<QuestionDto> findByTopicPoolId(Long topicPoolId) {
        return em.createQuery("select q from Question q where q.topicPool.id = :id", Question.class)
                .setParameter("id", topicPoolId)
                .getResultList()
                .stream()
                .map(QuestionMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Persitsts a new question (of any type) in the database.
     *
     * @param questionDto Dto containing full question details
     * @return the saved Question as Dto
     */
    @Transactional
    public QuestionDto create(QuestionDto questionDto) {
        Question question = QuestionMapper.toEntity(questionDto);

        if(questionDto.topicPool() != null && questionDto.topicPool().id() != null) {
            question.setTopicPool(em.getReference(TopicPool.class, questionDto.topicPool().id()));
        }

        em.persist(question);
        em.flush();

        return QuestionMapper.toDto(question);
    }
}
