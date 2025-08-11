package at.learnhub.repository;

import at.learnhub.dto.simple.UserSlimDto;
import at.learnhub.mapper.UserMapper;
import at.learnhub.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@ApplicationScoped
public class UserRepository {
    @Inject
    EntityManager em;

    public List<UserSlimDto> findAll() {
        return em.createQuery("SELECT u FROM User u", User.class)
                .getResultList()
                .stream()
                .map(UserMapper::toSlimDto)
                .toList();
    }

    public User getUserById(Long id) {
        User user =  em.find(User.class, id);
        if (user == null) {
            throw new EntityNotFoundException("User with id " + id + " not found.");
        }
        return user;
    }

    public List<UserSlimDto> findAllTeachers() {
        return em.createQuery("SELECT u FROM User u WHERE u.isTeacher = true", User.class)
                .getResultList()
                .stream()
                .map(UserMapper::toSlimDto)
                .toList();
    }

    public List<UserSlimDto> findActiveTeachers(int limit) {
        return em.createQuery("""
                        SELECT u
                        FROM User u
                        WHERE u.isTeacher = true
                        ORDER BY SIZE(u.ownedTopicContents) DESC
                        """, User.class)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(UserMapper::toSlimDto)
                .toList();
    }
}
