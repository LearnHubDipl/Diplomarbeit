package at.learnhub.repository;

import at.learnhub.model.StreakTracking;
import at.learnhub.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class StreakTrackingRepository {

    @Inject
    EntityManager em;

    public StreakTracking findByUser(User user) {
        List<StreakTracking> result = em.createQuery(
                        "SELECT s FROM StreakTracking s WHERE s.user = :user", StreakTracking.class)
                .setParameter("user", user)
                .getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    public User findById(Long id) {
        return em.find(User.class, id);
    }

    public void persist(StreakTracking streakTracking) {
        em.persist(streakTracking);
    }
}
