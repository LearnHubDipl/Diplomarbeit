package at.learnhub.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ExamQuestionId implements Serializable {
    private Long examId;
    private QuestionPoolEntryId entryId;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ExamQuestionId that = (ExamQuestionId) o;
        return Objects.equals(examId, that.examId) && Objects.equals(entryId, that.entryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(examId, entryId);
    }


    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public QuestionPoolEntryId getEntryId() {
        return entryId;
    }

    public void setEntryId(QuestionPoolEntryId entryId) {
        this.entryId = entryId;
    }
}

