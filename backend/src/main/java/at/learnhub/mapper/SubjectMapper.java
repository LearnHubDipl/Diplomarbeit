package at.learnhub.mapper;

import at.learnhub.dto.request.CreateSubjectRequestDto;
import at.learnhub.dto.request.UpdateSubjectRequestDto;
import at.learnhub.dto.simple.SubjectDto;
import at.learnhub.dto.simple.SubjectSlimDto;
import at.learnhub.dto.simple.TopicPoolSlimDto;
import at.learnhub.model.MediaFile;
import at.learnhub.model.Subject;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class SubjectMapper {

    private SubjectMapper() {}

    public static SubjectSlimDto toSlimDto(Subject s) {
        if (s == null) return null;
        return new SubjectSlimDto(
                s.getId(),
                s.getName(),
                s.getDescription(),
                s.getImg() // direkt das MediaFile setzen
        );
    }

    public static SubjectDto toDto(Subject s) {
        if (s == null) return null;

        List<TopicPoolSlimDto> topicPools = null;
        if (s.getTopicPools() != null) {
            topicPools = s.getTopicPools().stream()
                    .filter(Objects::nonNull)
                    .map(TopicPoolMapper::toSlimDto)
                    .collect(Collectors.toList());
        }

        return new SubjectDto(
                s.getId(),
                s.getName(),
                s.getDescription(),
                MediaFileMapper.toSlimDto(s.getImg()), // <- HIER: Slim statt Entity
                topicPools
        );
    }

    public static Subject fromCreateDto(CreateSubjectRequestDto dto, MediaFile imgEntityOrNull) {
        if (dto == null) return null;
        Subject s = new Subject();
        s.setName(dto.name());
        s.setDescription(dto.description());
        s.setImg(imgEntityOrNull);
        return s;
    }

    public static void applyUpdate(Subject s, UpdateSubjectRequestDto dto, MediaFile imgEntityOrNull) {
        if (s == null || dto == null) return;
        if (dto.name() != null) s.setName(dto.name());
        if (dto.description() != null) s.setDescription(dto.description());
        if (dto.imgId() != null) s.setImg(imgEntityOrNull);
    }
}
