package at.learnhub.repository;

import at.learnhub.dto.simple.SubjectDto;
import at.learnhub.mapper.SubjectMapper;
import at.learnhub.model.Subject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class SubjectRepository {

    @Inject
    EntityManager em;

    public List<SubjectDto> findAllOrderedByName() {
        return em.createQuery("SELECT s FROM Subject s ORDER BY LOWER(s.name)", Subject.class)
                .getResultList()
                .stream()
                .map(SubjectMapper::toDto)
                .collect(Collectors.toList());
    }

    public SubjectDto getDtoById(Long id) {
        return SubjectMapper.toDto(getById(id));
    }

    public Subject getById(Long id) {
        Subject subject = em.find(Subject.class, id);
        if (subject == null) {
            throw new EntityNotFoundException("Subject with id " + id + " not found.");
        }
        return subject;
    }

    public boolean existsByNameIgnoreCase(String name) {
        Long count = em.createQuery(
                        "SELECT COUNT(s) FROM Subject s WHERE LOWER(s.name) = LOWER(:name)", Long.class)
                .setParameter("name", name)
                .getSingleResult();
        return count != null && count > 0;
    }

    @Transactional
    public Subject create(Subject subject) {
        em.persist(subject);
        return subject;
    }

    @Transactional
    public Subject update(Subject subject) {
        return em.merge(subject);
    }

    @Transactional
    public void delete(Long id) {
        em.remove(getById(id));
    }
}
