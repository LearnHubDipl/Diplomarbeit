package at.learnhub.service;

import at.learnhub.model.Solution;
import at.learnhub.model.SolutionVote;
import at.learnhub.model.User;
import at.learnhub.repository.SolutionRepository;
import at.learnhub.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.ArrayList;

@ApplicationScoped
public class SolutionService {

    @Inject
    SolutionRepository solutionRepository;

    @Transactional
    public SolutionVote castVote(Long solutionId, Long userId, boolean isUpVote) {
        Solution solution = solutionRepository.findSolutionById(solutionId);
        User user = solutionRepository.findUserById(userId);

        if (solution == null || user == null) {
            throw new IllegalArgumentException("Solution or User not found");
        }

        SolutionVote existingVote = solutionRepository.findByUserAndSolution(userId, solutionId);
        if (existingVote != null) {
            existingVote.setUpVote(isUpVote); // Update Vote
            return solutionRepository.save(existingVote);
        }

        SolutionVote vote = new SolutionVote();
        vote.setSolution(solution);
        vote.setUser(user);
        vote.setUpVote(isUpVote);

        return solutionRepository.save(vote);
    }


    public int getVoteCount(Long solutionId) {
        return solutionRepository.findBySolutionId(solutionId).stream()
                .mapToInt(v -> v.getUpVote() ? 1 : -1)
                .sum();
    }


}
