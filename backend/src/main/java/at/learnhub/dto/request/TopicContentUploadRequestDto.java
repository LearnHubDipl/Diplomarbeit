package at.learnhub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record TopicContentUploadRequestDto(
        @NotBlank @Size(min = 3, max = 180) String title,
        @Size(max = 2000) String description,
        @NotBlank @Size(min = 2, max = 120) String uploaderName, // wird für Mail/Text genutzt
        Long subjectId,   // vorhanden ODER newSubjectName + newTopicNames
        Long topicPoolId, // optional
        Long teacherId,   // Pflicht: ausgewählter Lehrer
        @Size(min = 2, max = 120) String newSubjectName,
        List<@Size(min = 2, max = 120) String> newTopicNames // max. 10 serverseitig prüfen
) {}
