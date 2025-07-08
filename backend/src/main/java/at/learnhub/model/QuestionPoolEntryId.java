package at.learnhub.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class QuestionPoolEntryId implements Serializable {
    private Long questionId;
    private Long poolId;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        QuestionPoolEntryId that = (QuestionPoolEntryId) o;
        return Objects.equals(questionId, that.questionId) && Objects.equals(poolId, that.poolId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(questionId, poolId);
    }


    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Long getPoolId() {
        return poolId;
    }

    public void setPoolId(Long poolId) {
        this.poolId = poolId;
    }
}
