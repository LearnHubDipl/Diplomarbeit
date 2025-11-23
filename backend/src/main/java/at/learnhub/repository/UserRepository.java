package at.learnhub.repository;

import at.learnhub.dto.simple.UserSlimDto;
import at.learnhub.mapper.UserMapper;
import at.learnhub.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

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
        User user = em.find(User.class, id);
        if (user == null) {
            throw new EntityNotFoundException("User with id " + id + " not found.");
        }
        return user;
    }

    public UserSlimDto getUserSlimDtoById(Long id) {
        return UserMapper.toSlimDto(getUserById(id));
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
            SELECT u FROM User u 
            WHERE u.isTeacher = true 
            ORDER BY SIZE(u.ownedTopicContents) DESC
            """, User.class)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(UserMapper::toSlimDto)
                .toList();
    }

    /**
     * Finds a user by Keycloak sub.
     * @param keycloakSub the Keycloak sub
     * @return Optional containing the UserSlimDto if found
     */
    public Optional<UserSlimDto> findByKeycloakSub(String keycloakSub) {
        try {
            User user = em.createNamedQuery(User.FIND_BY_KEYCLOAK_SUB, User.class)
                    .setParameter("sub", keycloakSub)
                    .getSingleResult();
            return Optional.of(UserMapper.toSlimDto(user));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Finds a user entity by Keycloak sub.
     * @param keycloakSub the Keycloak sub
     * @return Optional containing the User entity if found
     */
    public Optional<User> findUserEntityByKeycloakSub(String keycloakSub) {
        try {
            User user = em.createNamedQuery(User.FIND_BY_KEYCLOAK_SUB, User.class)
                    .setParameter("sub", keycloakSub)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public Optional<UserSlimDto> findByEmail(String email) {
        try {
            User user = em.createNamedQuery(User.FIND_BY_EMAIL, User.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(UserMapper.toSlimDto(user));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    /**
     * Creates and persists a new user.
     * @param user the user entity to persist
     * @return the persisted user
     */
    @Transactional
    public User createUser(User user) {
        em.persist(user);
        return user;
    }

    /**
     * Updates an existing user.
     * @param user the user entity to update
     * @return the updated user
     */
    @Transactional
    public User updateUser(User user) {
        return em.merge(user);
    }
}