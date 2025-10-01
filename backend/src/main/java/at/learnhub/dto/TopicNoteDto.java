package at.learnhub.dto;

import java.time.LocalDate;

public class TopicNoteDto {
    public Long id;
    public String title;
    public String description;
    public LocalDate date;

    public String pdfUrl;
    public String uploaderName;
    public Long teacherId;
    public String teacherName;

    public Long subjectId;
    public Long topicPoolId;
}