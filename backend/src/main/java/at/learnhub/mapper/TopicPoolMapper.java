package at.learnhub.mapper;

import at.learnhub.dto.simple.TopicPoolSlimDto;
import at.learnhub.model.TopicPool;

public class TopicPoolMapper {

    public static TopicPoolSlimDto toSlimDto(TopicPool topicPool) {
        return new TopicPoolSlimDto(topicPool.getId(), topicPool.getName(), topicPool.getDescription());
    }

}
