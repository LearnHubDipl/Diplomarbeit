package at.learnhub.mapper;

import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.dto.simple.QuestionSlimDto;
import at.learnhub.dto.simple.SolutionSlimDto;
import at.learnhub.model.Question;
import at.learnhub.model.Solution;
import at.learnhub.model.SolutionVote;


public class SolutionMapper {

    /*
    public static SolutionDto toDto(Question question) {
        return new QuestionDto(question.getId(), question.getText(), question.getExplanation(),
                question.getMedia(), question.getType(), question.getDifficulty(),
                question.getPublic(), TopicPoolMapper.toSlimDto(question.getTopicPool()),
                question.getAnswers().stream().map(AnswerMapper::toSlimDto).toList(),
                );
    }*/

    public static SolutionSlimDto toSlimDto(Solution solution) {
        long score = 0L;
        if (solution.getVotes() != null) {
            for (SolutionVote vote : solution.getVotes()) {
                // +1 für Upvote, -1 für Downvote laut deiner Schema-Beschreibung
                if (Boolean.TRUE.equals(vote.getUpVote())) {
                    score++;
                } else {
                    score--;
                }
            }
        }

        return new SolutionSlimDto(
                solution.getId(),
                solution.getSteps().stream().map(SolutionStepMapper::toDto).toList(),
                score
        );
    }

    /*
    public static Solution toEntity(SolutionDto solutionDto) {
        Solution solution = new Solution();
        solution.setId(solutionDto.id());
        return question;
    }*/

}
