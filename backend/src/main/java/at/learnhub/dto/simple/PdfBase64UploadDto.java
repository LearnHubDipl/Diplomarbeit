package at.learnhub.dto.simple;

public class PdfBase64UploadDto {
    public String title;
    public Long subjectId;
    public Long topicPoolId;
    public Long uploaderUserId;
    public String base64;
    public String fileName;

    public PdfBase64UploadDto() {}
}
