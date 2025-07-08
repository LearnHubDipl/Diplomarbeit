package at.learnhub.repository;

import at.learnhub.model.Subject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class SubjectRepository {
    @Inject
    private EntityManager em;

    public List<Subject> findAll() {
        return em.createQuery("select s from Subject s", Subject.class).getResultList();
    }
}
