package at.learnhub.mapper;

import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.dto.simple.SolutionSlimDto;
import at.learnhub.dto.simple.SolutionStepDto;
import at.learnhub.model.Question;
import at.learnhub.model.Solution;
import at.learnhub.model.SolutionStep;


public class SolutionStepMapper {

    public static SolutionStepDto toDto(SolutionStep step) {
        return new SolutionStepDto(step.getId(), step.getTitle(), step.getText());
    }

}
