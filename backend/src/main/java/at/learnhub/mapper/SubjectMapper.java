package at.learnhub.mapper;

import at.learnhub.dto.simple.SubjectDto;
import at.learnhub.dto.simple.SubjectSlimDto;
import at.learnhub.model.Subject;

public class SubjectMapper {

    public static SubjectDto toDto(Subject subject) {
        return new SubjectDto(subject.getId(), subject.getName(),
                subject.getDescription(), subject.getImg(),
                subject.getTopicPools().stream().map(TopicPoolMapper::toSlimDto).toList());
    }

    public static SubjectSlimDto toSlimDto(Subject subject) {
        return new SubjectSlimDto(subject.getId(), subject.getName(),
                subject.getDescription(), subject.getImg());
    }

    public static Subject toEntity(SubjectDto subjectDto) {
        Subject subject = new Subject();
        subject.setId(subjectDto.id());
        subject.setName(subjectDto.name());
        subject.setDescription(subjectDto.description());
        subject.setImg(subjectDto.img());
        return subject;
    }

}
