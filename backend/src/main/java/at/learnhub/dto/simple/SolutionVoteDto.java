package at.learnhub.dto.simple;

public record SolutionVoteDto(Long id, boolean upVote, Long solutionId, Long userId) {
}
