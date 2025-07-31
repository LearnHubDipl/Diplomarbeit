package at.learnhub.mapper;

import at.learnhub.dto.simple.QuestionDto;
import at.learnhub.dto.simple.QuestionPoolDto;
import at.learnhub.dto.simple.QuestionSlimDto;
import at.learnhub.dto.simple.SolutionSlimDto;
import at.learnhub.model.Question;
import at.learnhub.model.QuestionPool;
import at.learnhub.model.QuestionType;

import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

public class QuestionPoolMapper {

    public static QuestionPoolDto toDto(QuestionPool pool) {
        return new QuestionPoolDto(pool.getId(), UserMapper.toSlimDto(pool.getUser()),
                pool.getEntries().stream().map(QuestionPoolEntryMapper::toSlimDto).toList());
    }

}
