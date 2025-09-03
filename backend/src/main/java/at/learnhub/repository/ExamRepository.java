package at.learnhub.repository;

import at.learnhub.dto.simple.ExamDto;
import at.learnhub.dto.simple.SubjectDto;
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
}
