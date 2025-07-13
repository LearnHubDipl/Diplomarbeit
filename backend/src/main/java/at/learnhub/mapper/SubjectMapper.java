package at.learnhub.mapper;

import at.learnhub.dto.simple.SubjectDto;
import at.learnhub.dto.simple.SubjectSlimDto;
import at.learnhub.model.Subject;

/**
 * Utility class responsible for converting between {@link Subject} entities and their DTO representations.
 * <p>
 * This includes:
 * <ul>
 *     <li>{@link SubjectDto} — full DTO including topic pools</li>
 *     <li>{@link SubjectSlimDto} — simplified DTO excluding topic pools</li>
 * </ul>
 *
 * This class is stateless and should not be instantiated.
 */
public class SubjectMapper {

    /**
     * Maps a Subject entity to a full SubjectDto including topic pools.
     *
     * @param subject the Subject entity
     * @return the corresponding SubjectDto
     */
    public static SubjectDto toDto(Subject subject) {
        return new SubjectDto(subject.getId(), subject.getName(),
                subject.getDescription(), subject.getImg(),
                subject.getTopicPools().stream().map(TopicPoolMapper::toSlimDto).toList());
    }

    /**
     * Maps a Subject entity to a slim SubjectSlimDto, excluding topic pools.
     *
     * @param subject the Subject entity
     * @return the slim SubjectSlimDto
     */
    public static SubjectSlimDto toSlimDto(Subject subject) {
        return new SubjectSlimDto(subject.getId(), subject.getName(),
                subject.getDescription(), subject.getImg());
    }

    /**
     * Returns a new Subject created from the information contained in the provided DTO.
     *
     * @param subjectDto the Subject DTO
     * @return the newly created Subject
     */
    public static Subject toEntity(SubjectDto subjectDto) {
        Subject subject = new Subject();
        subject.setId(subjectDto.id());
        subject.setName(subjectDto.name());
        subject.setDescription(subjectDto.description());
        subject.setImg(subjectDto.img());
        return subject;
    }

}
