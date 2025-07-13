package at.learnhub.repository;

import at.learnhub.dto.simple.SubjectDto;
import at.learnhub.mapper.SubjectMapper;
import at.learnhub.model.Subject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class SubjectRepository {
    @Inject
    EntityManager em;

    public List<SubjectDto> findAll() {
        return em.createQuery("select s from Subject s", Subject.class).getResultList()
                .stream().map(SubjectMapper::toDto).toList();
    }
}
