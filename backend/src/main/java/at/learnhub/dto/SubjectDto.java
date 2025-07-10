package at.learnhub.dto;

import at.learnhub.model.MediaFile;

public record SubjectDto(String name, String description, MediaFile img) {
}
