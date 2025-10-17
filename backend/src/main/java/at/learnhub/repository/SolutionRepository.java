package at.learnhub.repository;

import at.learnhub.model.Solution;
import at.learnhub.model.SolutionVote;
import at.learnhub.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.util.List;


@ApplicationScoped
public class SolutionRepository {

    @Inject
    EntityManager entityManager;

    public SolutionVote save(SolutionVote vote) {
        if (vote.getId() == null) {
            entityManager.persist(vote);
            return vote;
        } else {
            return entityManager.merge(vote);
        }
    }

    public SolutionVote findById(Long id) {
        return entityManager.find(SolutionVote.class, id);
    }

    public Solution findSolutionById(Long id) {
        return entityManager.find(Solution.class, id);
    }

    public User findUserById(Long id) {
        return entityManager.find(User.class, id);
    }

    public List<SolutionVote> findBySolutionId(Long solutionId) {
        return entityManager.createQuery(
                        "SELECT v FROM SolutionVote v WHERE v.solution.id = :solutionId", SolutionVote.class)
                .setParameter("solutionId", solutionId)
                .getResultList();
    }

    public SolutionVote findByUserAndSolution(Long userId, Long solutionId) {
        List<SolutionVote> votes = entityManager.createQuery(
                        "SELECT v FROM SolutionVote v WHERE v.user.id = :userId AND v.solution.id = :solutionId",
                        SolutionVote.class)
                .setParameter("userId", userId)
                .setParameter("solutionId", solutionId)
                .getResultList();
        return votes.isEmpty() ? null : votes.get(0);
    }

    public void delete(SolutionVote vote) {
        if (!entityManager.contains(vote)) {
            vote = entityManager.merge(vote);
        }
        entityManager.remove(vote);
    }
}