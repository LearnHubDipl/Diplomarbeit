package at.learnhub.repository;

import at.learnhub.dto.request.QuestionCreationRequestDto;
import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.dto.simple.QuestionPoolDto;
import at.learnhub.dto.simple.QuestionUpdateRequestDto;
import at.learnhub.mapper.QuestionMapper;
import at.learnhub.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class QuestionRepository {
    @Inject
    EntityManager em;

    private static final int LOCK_MINUTES_ONCE = 1440;   // 1 Tag
    private static final int LOCK_MINUTES_TWICE = 4320;  // 3 Tage

    public List<QuestionDto> findAll() {
        return em.createQuery("SELECT q FROM Question q", Question.class)
                .getResultList()
                .stream()
                .map(QuestionMapper::toDto)
                .collect(Collectors.toList());
    }


    public QuestionDto getQuestionDtoById(Long id) {
        Question question = getQuestionById(id);
        return QuestionMapper.toDto(question);
    }

    public Question getQuestionById(Long id) {
        Question question = em.find(Question.class, id);
        if (question == null) {
            throw new EntityNotFoundException("Question with id " + id + " not found.");
        }
        return question;
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
        if (difficulty < 1 || difficulty > 3) throw new BadRequestException("Difficulty must be between 1 and 3");

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
     * @param question Dto containing full question details
     * @return the saved Question as Dto
     */
    @Transactional
    public Question create(Question question) {
        em.persist(question);

        return question;
    }

    /**
     * Question gets updated
     * @param id
     * @param dto
     * @return
     */
    @Transactional
    public Question updateQuestion(Long id, QuestionUpdateRequestDto dto) {
        Question existing = em.find(Question.class, id);
        if (existing == null) {
            throw new EntityNotFoundException("Question with id " + id + " not found.");
        }

        if (dto.text() != null) {
            existing.setText(dto.text());
        }
        if (dto.explanation() != null) {
            existing.setExplanation(dto.explanation());
        }
        if (dto.type() != null) {
            existing.setType(dto.type());
        }
        if (dto.isPublic() != null) {
            existing.setPublic(dto.isPublic());
        }

        if (dto.answers() != null) {
            existing.getAnswers().clear();
            dto.answers().forEach(answerDto -> {
                Answer answer = new Answer();
                answer.setText(answerDto.text());
                answer.setCorrect(answerDto.isCorrect());
                answer.setQuestion(existing);
                existing.getAnswers().add(answer);
            });


        }

        return em.merge(existing);
    }

    @Transactional
    public void deleteQuestion(Long id) {
        Question question = em.find(Question.class, id);
        if (question == null) {
            throw new EntityNotFoundException("Question with id " + id + " not found.");
        }
        em.remove(question);
    }


    public List<Long> findIdsByUserAndTopicPool(Long userId, Long topicPoolId) {

        String jpql = """
            SELECT e.question.id,
                   COALESCE(e.correctCount, 0) as cc,
                   e.lastAnsweredCorrectly as lac,
                   e.answeredAt as answeredAt
            FROM QuestionPoolEntry e 
            JOIN e.questionPool p 
            WHERE p.user.id = :userId
            AND COALESCE(e.correctCount, 0) < 3
        """;

        if (topicPoolId != null) {
            jpql += " AND e.question.topicPool.id = :topicPoolId";
        }

        jpql += """
            ORDER BY 
                CASE 
                    WHEN e.lastAnsweredCorrectly IS NULL OR COALESCE(e.correctCount, 0) = 0 THEN 1
                    WHEN e.lastAnsweredCorrectly = false THEN 2
                    WHEN COALESCE(e.correctCount, 0) = 1 THEN 3
                    WHEN COALESCE(e.correctCount, 0) = 2 THEN 4
                    ELSE 5
                END,
                e.answeredAt NULLS FIRST
        """;

        var query = em.createQuery(jpql, Object[].class)
                .setParameter("userId", userId);

        if (topicPoolId != null) {
            query.setParameter("topicPoolId", topicPoolId);
        }

        LocalDateTime now = LocalDateTime.now();

        return query.getResultList()
                .stream()
                .filter(row -> {
                    int correctCount = ((Number) row[1]).intValue();
                    Boolean lastAnsweredCorrectly = (Boolean) row[2];
                    LocalDateTime answeredAt = (LocalDateTime) row[3];

                    if (lastAnsweredCorrectly == null || !lastAnsweredCorrectly) {
                        return true;
                    }

                    if (answeredAt != null) {
                        int lockMinutes = (correctCount == 1) ? LOCK_MINUTES_ONCE
                                : (correctCount == 2) ? LOCK_MINUTES_TWICE
                                : 0;
                        if (lockMinutes > 0) {
                            LocalDateTime unlockTime = answeredAt.plusMinutes(lockMinutes);
                            return now.isAfter(unlockTime);
                        }
                    }

                    return true;
                })
                .map(row -> (Long) row[0])
                .collect(Collectors.toList());
    }
    /**
     * Creates a new question with answers from the creation DTO
     *
     * @param dto the creation request DTO
     * @return the created Question entity
     */
    @Transactional
    public Question createQuestion(QuestionCreationRequestDto dto) {
        System.out.println("[QuestionRepository] Creating question from DTO");
        System.out.println("[QuestionRepository] User ID: " + dto.userId());
        System.out.println("[QuestionRepository] TopicPool ID: " + dto.topicPoolId());

        User user = em.find(User.class, dto.userId());
        if (user == null) {
            throw new EntityNotFoundException("User not found with ID: " + dto.userId());
        }
        System.out.println("[QuestionRepository] User found: " + user.getName());

        TopicPool topicPool = em.find(TopicPool.class, dto.topicPoolId());
        if (topicPool == null) {
            throw new EntityNotFoundException("Topic pool not found with ID: " + dto.topicPoolId());
        }
        System.out.println("[QuestionRepository] TopicPool found: " + topicPool.getName());

        Question question = new Question();
        question.setText(dto.text());
        question.setExplanation(dto.explanation());
        question.setType(dto.type());
        question.setDifficulty(dto.difficulty());
        question.setPublic(dto.isPublic() != null ? dto.isPublic() : false);
        question.setUser(user);
        question.setTopicPool(topicPool);

        question.setAnswers(new ArrayList<>());
        question.setSolutions(new ArrayList<>());
        question.setEntries(new ArrayList<>());

        em.persist(question);
        em.flush();

        System.out.println("[QuestionRepository] Question persisted with ID: " + question.getId());

        if (dto.answers() != null && !dto.answers().isEmpty()) {
            System.out.println("[QuestionRepository] Adding " + dto.answers().size() + " answers");

            for (at.learnhub.dto.request.AnswerCreationRequestDto answerDto : dto.answers()) {
                Answer answer = new Answer();
                answer.setText(answerDto.text());
                answer.setCorrect(answerDto.isCorrect() != null ? answerDto.isCorrect() : false);
                answer.setQuestion(question);

                em.persist(answer);
                question.getAnswers().add(answer);
            }

            em.flush();
            System.out.println("[QuestionRepository] Answers added successfully");
        }

        System.out.println("[QuestionRepository] Question created successfully with ID: " + question.getId());

        return question;
    }
}
