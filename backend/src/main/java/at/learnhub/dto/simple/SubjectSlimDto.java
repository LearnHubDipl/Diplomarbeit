package at.learnhub.dto.simple;

import at.learnhub.model.MediaFile;

public record SubjectSlimDto(Long id, String name, String description, MediaFile img) {
}
