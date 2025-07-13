package at.learnhub.dto.simple;

import at.learnhub.model.MediaFile;

import java.util.List;

public record SubjectDto(Long id, String name, String description, MediaFile img, List<TopicPoolSlimDto> topicPools) {
}
