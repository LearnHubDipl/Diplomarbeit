package at.learnhub.service;

import at.learnhub.dto.simple.SolutionSlimDto;
import at.learnhub.dto.simple.SolutionVoteCountDto;
import at.learnhub.model.*;
import at.learnhub.repository.QuestionRepository;
import at.learnhub.repository.SolutionRepository;
import at.learnhub.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class SolutionService {

    @Inject
    SolutionRepository solutionRepository;

    @Inject
    QuestionRepository questionRepository;

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


    public SolutionVoteCountDto getVoteCounts(Long solutionId) {
        List<SolutionVote> votes = solutionRepository.findBySolutionId(solutionId);
        long ups = votes.stream().filter(v -> Boolean.TRUE.equals(v.getUpVote())).count();
        long downs = votes.stream().filter(v -> Boolean.FALSE.equals(v.getUpVote())).count();

        return new SolutionVoteCountDto(solutionId, ups, downs);
    }



    @Transactional
    public Solution createSolution(Long questionId, Long userId, SolutionSlimDto dto) {
        // Nutzt jetzt die saubere Methode aus dem QuestionRepo
        Question question = questionRepository.getQuestionById(questionId);

        // User-Suche (idealerweise auch über ein UserRepository)
        User user = solutionRepository.findUserById(userId);
        if (user == null) throw new EntityNotFoundException("User not found");

        Solution solution = new Solution();
        solution.setQuestion(question);
        solution.setUser(user);

        // Mapping der Schritte
        List<SolutionStep> steps = dto.steps().stream().map(stepDto -> {
            SolutionStep step = new SolutionStep();
            step.setTitle(stepDto.title());
            step.setText(stepDto.text());
            step.setSolution(solution);
            return step;
        }).toList();

        solution.setSteps(steps);

        return solutionRepository.saveSolution(solution);
    }

}
