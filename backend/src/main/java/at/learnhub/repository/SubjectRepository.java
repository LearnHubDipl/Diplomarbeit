package at.learnhub.repository;

import at.learnhub.dto.simple.SubjectDto;
import at.learnhub.mapper.SubjectMapper;
import at.learnhub.model.Subject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Repository class for accessing {@link Subject} data from the database.
 * Handles fetching and converting Subject entities to DTOs.
 */
@ApplicationScoped
public class SubjectRepository {
    @Inject
    EntityManager em;

    /**
     * Retrieves all {@link Subject} entities from the database and maps them to {@link SubjectDto} objects.
     *
     * @return a list of all subjects as DTOs, including their related topic pools
     */
    public List<SubjectDto> findAll() {
        return em.createQuery("select s from Subject s", Subject.class).getResultList()
                .stream().map(SubjectMapper::toDto).toList();
    }
}
