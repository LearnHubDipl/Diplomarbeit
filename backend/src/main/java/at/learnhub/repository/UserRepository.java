package at.learnhub.repository;

import at.learnhub.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

@ApplicationScoped
public class UserRepository {
    @Inject
    EntityManager em;

    public User getUserById(Long id) {
        User user =  em.find(User.class, id);
        if (user == null) {
            throw new EntityNotFoundException("User with id " + id + " not found.");
        }
        return user;
    }
}
