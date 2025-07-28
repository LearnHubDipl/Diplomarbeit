package at.learnhub.dto.simple;

public record AnswerSlimDto(
        Long id,
        String text,
        Boolean isCorrect
) {
}