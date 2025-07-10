package at.learnhub.repository;

import at.learnhub.dto.SubjectDto;
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
        return em.createQuery("select new at.learnhub.dto.SubjectDto(s.name, s.description, s.img) from Subject s", SubjectDto.class).getResultList();
    }
}
