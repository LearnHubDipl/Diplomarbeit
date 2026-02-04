package at.learnhub.repository;

import at.learnhub.dto.simple.ExamDto;
import at.learnhub.dto.simple.ExamHistoryDto;
import at.learnhub.dto.simple.SubjectDto;
import at.learnhub.dto.simple.UserExamAverageDto;
import at.learnhub.mapper.ExamMapper;
import at.learnhub.mapper.SubjectMapper;
import at.learnhub.model.Exam;
import at.learnhub.model.Subject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class ExamRepository {

    @Inject
    EntityManager em;

    public List<ExamDto> findAll() {
        return em.createQuery("SELECT e FROM Exam e", Exam.class)
                .getResultList()
                .stream().map(ExamMapper::toDto).toList();
    }

    public ExamDto getDtoById(Long id) {
        return ExamMapper.toDto(getEntityById(id));
    }

    public Exam getEntityById(Long id) {
        Exam exam = em.find(Exam.class, id);
        if (exam == null) {
            throw new EntityNotFoundException("Exam with id " + id + " not found.");
        }
        return exam;
    }

    public List<ExamDto> findByUserId(Long userId) {
        return em.createQuery("SELECT e FROM Exam e WHERE e.user.id = :userId ORDER BY e.startedAt DESC", Exam.class)
                .setParameter("userId", userId)
                .getResultList()
                .stream()
                .map(ExamMapper::toDto)
                .toList();
    }

    public UserExamAverageDto findAverageAndCountByUserId(Long userId) {
        Object[] result = (Object[]) em.createQuery(
                        "SELECT AVG(e.score), COUNT(e) FROM Exam e WHERE e.user.id = :userId")
                .setParameter("userId", userId)
                .getSingleResult();

        Number avgNumber = (Number) result[0];
        Long count = (Long) result[1];

        Double average = (avgNumber == null) ? null : avgNumber.doubleValue();

        return new UserExamAverageDto(userId, average, count);
    }

    public List<ExamHistoryDto> findHistoryByUserId(Long userId) {
        List<Exam> entities = em.createQuery(
                        "SELECT DISTINCT e FROM Exam e " +
                                "LEFT JOIN FETCH e.topicPools tp " +
                                "LEFT JOIN FETCH tp.subject s " +
                                "WHERE e.user.id = :userId ORDER BY e.startedAt DESC",
                        Exam.class)
                .setParameter("userId", userId)
                .getResultList();

        return entities.stream()
                .map(ExamMapper::toHistoryDto)
                .toList();
    }
}
