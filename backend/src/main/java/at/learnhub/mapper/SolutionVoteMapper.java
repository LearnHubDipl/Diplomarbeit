package at.learnhub.mapper;

import at.learnhub.dto.simple.SolutionSlimDto;
import at.learnhub.dto.simple.SolutionVoteSlimDto;
import at.learnhub.model.Solution;
import at.learnhub.model.SolutionVote;


public class SolutionVoteMapper {

    /*
    public static SolutionVote toDto(SolutionVote vote) {
        return new QuestionDto(question.getId(), question.getText(), question.getExplanation(),
                question.getMedia(), question.getType(), question.getDifficulty(),
                question.getPublic(), TopicPoolMapper.toSlimDto(question.getTopicPool()),
                question.getAnswers().stream().map(AnswerMapper::toSlimDto).toList(),
                );
    }*/

    public static SolutionVoteSlimDto toSlimDto(SolutionVote vote) {
        return new SolutionVoteSlimDto(vote.getId(), vote.getUpVote());
    }

    /*
    public static SolutionVote toEntity(SolutionVoteDto solutionVoteDto) {
        Solution solution = new Solution();
        solution.setId(solutionDto.id());
        return question;
    }*/

}
