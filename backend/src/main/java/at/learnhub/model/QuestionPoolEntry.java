package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "question_pool_entry")
public class QuestionPoolEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "answered_at")
    private LocalDateTime answeredAt;
    @Column(name = "last_answered_correctly")
    private Boolean lastAnsweredCorrectly;
    @Column(name = "correct_count")
    private Integer correctCount;


    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonIgnoreProperties({"entries", "user"})
    private Question question;

    @ManyToOne
    @JoinColumn(name = "pool_id")
    @JsonIgnoreProperties({"entries", "user"})
    private QuestionPool questionPool;

    @OneToMany(mappedBy = "entry")
    @JsonIgnoreProperties({"entry"})
    private List<ExamQuestion> examQuestions;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
    }

    public Boolean getLastAnsweredCorrectly() {
        return lastAnsweredCorrectly;
    }

    public void setLastAnsweredCorrectly(Boolean lastAnsweredCorrectly) {
        this.lastAnsweredCorrectly = lastAnsweredCorrectly;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(Integer correctCount) {
        this.correctCount = correctCount;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public QuestionPool getQuestionPool() {
        return questionPool;
    }

    public void setQuestionPool(QuestionPool questionPool) {
        this.questionPool = questionPool;
    }

    public List<ExamQuestion> getExamQuestions() {
        return examQuestions;
    }

    public void setExamQuestions(List<ExamQuestion> examQuestions) {
        this.examQuestions = examQuestions;
    }
}
