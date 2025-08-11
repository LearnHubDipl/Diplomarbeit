package at.learnhub.mapper;

import at.learnhub.dto.simple.SubjectSlimDto;
import at.learnhub.dto.simple.TopicPoolSlimDto;
import at.learnhub.model.TopicPool;

public class TopicPoolMapper {

    private TopicPoolMapper() {}

    public static TopicPoolSlimDto toSlimDto(TopicPool t) {
        if (t == null) return null;
        return new TopicPoolSlimDto(
                t.getId(),
                t.getName(),
                t.getDescription(),
                SubjectMapper.toSlimDto(t.getSubject())
        );
    }
}
