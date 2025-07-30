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
        Long upvoteCount = 0L;
        for (SolutionVote currentSolution : solution.getVotes()) {
            if (currentSolution.getUpVote()) upvoteCount++;
            else upvoteCount--;
        }

        return new SolutionSlimDto(solution.getId(),
                solution.getSteps().stream().map(SolutionStepMapper::toDto).toList(),
                upvoteCount);
    }

    /*
    public static Solution toEntity(SolutionDto solutionDto) {
        Solution solution = new Solution();
        solution.setId(solutionDto.id());
        return question;
    }*/

}
