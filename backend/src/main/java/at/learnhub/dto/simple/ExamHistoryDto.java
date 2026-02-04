package at.learnhub.dto.simple;

import java.time.LocalDateTime;
import java.util.List;

public record ExamHistoryDto(
        Long id,
        Double score,
        Integer questionCount,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        List<String> subjectNames
) {}