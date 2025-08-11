package at.learnhub.repository;

import at.learnhub.model.MediaFile;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class MediaFileRepository {

    @Inject
    EntityManager em;

    public MediaFile getById(Long id) {
        MediaFile mf = em.find(MediaFile.class, id);
        if (mf == null) {
            throw new EntityNotFoundException("MediaFile with id " + id + " not found.");
        }
        return mf;
    }

    @Transactional
    public MediaFile create(MediaFile mf) {
        em.persist(mf);
        return mf;
    }

    @Transactional
    public MediaFile update(MediaFile mf) {
        return em.merge(mf);
    }

    @Transactional
    public void delete(Long id) {
        MediaFile mf = getById(id);
        em.remove(mf);
    }
}
