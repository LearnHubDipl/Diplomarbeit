package at.learnhub.mapper;

import at.learnhub.dto.simple.SubjectDto;
import at.learnhub.dto.simple.SubjectSlimDto;
import at.learnhub.dto.simple.TopicPoolDto;
import at.learnhub.dto.simple.TopicPoolSlimDto;
import at.learnhub.model.Subject;
import at.learnhub.model.TopicPool;

// TODO: update documentation when TopicPoolDto exists
/**
 * Utility class responsible for converting between {@link TopicPool} entities and their DTO representations.
 * <p>
 * This includes:
 * <ul>
 *     <li>TopicPoolDto (This doesn't exist yet) — full DTO including topic pools</li>
 *     <li>{@link TopicPoolSlimDto} — simplified DTO excluding topic pools</li>
 * </ul>
 *
 * This class is stateless and should not be instantiated.
 */
public class TopicPoolMapper {

    public static TopicPoolDto toDto(TopicPool topicPool) {
        return new TopicPoolDto(
                topicPool.getId(),
                topicPool.getName(),
                topicPool.getDescription(),
                SubjectMapper.toSlimDto(topicPool.getSubject())
        );
    }

    /**
     * Maps a {@link TopicPool} entity to a slim {@link TopicPoolSlimDto}, excluding all relations.
     *
     * @param topicPool the {@link TopicPool} entity
     * @return the slim {@link TopicPoolSlimDto}
     */
    public static TopicPoolSlimDto toSlimDto(TopicPool topicPool) {
        return new TopicPoolSlimDto(
                topicPool.getId(),
                topicPool.getName(),
                topicPool.getDescription(),
                SubjectMapper.toSlimDto(topicPool.getSubject())
        );
    }

}
