package at.learnhub.dto.simple;

import java.util.List;

public record ProgressLevelDto(String title, List<ProgressEntryDto> entries) {}
