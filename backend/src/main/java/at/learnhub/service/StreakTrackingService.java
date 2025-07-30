package at.learnhub.service;

import at.learnhub.model.StreakTracking;
import at.learnhub.model.User;
import at.learnhub.repository.StreakTrackingRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;

@ApplicationScoped
public class StreakTrackingService {
    @Inject
    StreakTrackingRepository streakTrackingRepository;


    public StreakTracking findByUser(User user) {
        return streakTrackingRepository.findByUser(user);
    }

    @Transactional
    public StreakTracking updateStreakOnLogin(User user) {
        StreakTracking tracking = streakTrackingRepository.findByUser(user);
        LocalDate today = LocalDate.now();

        if (tracking == null) {
            tracking = new StreakTracking();
            tracking.setUser(user);
            tracking.setStreak(1);
            tracking.setLastLoggedInAt(today);
            streakTrackingRepository.persist(tracking);
            return tracking;
        }

        LocalDate lastLogin = tracking.getLastLoggedInAt();

        if (lastLogin.isEqual(today)) {
            return tracking;
        }
        if (lastLogin.plusDays(1).isEqual(today)) {
            tracking.setStreak(tracking.getStreak() + 1);
        } else {
            tracking.setStreak(1);
        }

        tracking.setLastLoggedInAt(today);

        streakTrackingRepository.persist(tracking);
        return tracking;
    }

}
