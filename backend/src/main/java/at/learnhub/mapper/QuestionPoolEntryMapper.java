package at.learnhub.mapper;

import at.learnhub.dto.simple.QuestionPoolDto;
import at.learnhub.dto.simple.QuestionPoolEntrySlimDto;
import at.learnhub.model.QuestionPool;
import at.learnhub.model.QuestionPoolEntry;

public class QuestionPoolEntryMapper {


    public static QuestionPoolEntrySlimDto toSlimDto(QuestionPoolEntry entry) {
        return new QuestionPoolEntrySlimDto(entry.getId(), entry.getAnsweredAt(),
                entry.getLastAnsweredCorrectly(), entry.getCorrectCount(),
                QuestionMapper.toSlimDto(entry.getQuestion()));
    }

}
