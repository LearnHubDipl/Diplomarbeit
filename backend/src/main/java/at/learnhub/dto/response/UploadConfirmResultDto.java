package at.learnhub.dto.response;

public record UploadConfirmResultDto(
        String status,  // z.B. "CONFIRMED", "EXPIRED", "INVALID"
        String message
) {}
