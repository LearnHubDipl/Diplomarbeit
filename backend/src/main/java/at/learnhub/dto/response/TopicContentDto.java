package at.learnhub.dto.response;

import java.time.LocalDate;

public record TopicContentDto(
        Long id,
        String title,
        String description,
        String uploaderName,
        LocalDate uploadDate,
        Long teacherId,
        String teacherName,
        String pdfUrl,
        Long subjectId,
        Long topicPoolId,
        Boolean approved
) {}
