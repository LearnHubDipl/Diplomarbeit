package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "exam_question", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"exam_id", "entry_id"})
})
public class ExamQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    @JsonIgnoreProperties("questions")
    private Exam exam;

    @ManyToOne
    @JoinColumn(name = "entry_id", nullable = false)
    @JsonIgnoreProperties("examQuestions")
    private QuestionPoolEntry entry;

    @ManyToMany
    @JoinTable(
            name = "exam_question_selected_answers",
            joinColumns = @JoinColumn(name = "exam_question_id"),
            inverseJoinColumns = @JoinColumn(name = "answer_id")
    )
    @JsonIgnoreProperties("examQuestions")
    private List<Answer> selectedAnswers;

    @Column(name = "freet_text_answer")
    private String freeTextAnswer;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public QuestionPoolEntry getEntry() {
        return entry;
    }

    public void setEntry(QuestionPoolEntry entry) {
        this.entry = entry;
    }

    public List<Answer> getSelectedAnswers() {
        return selectedAnswers;
    }

    public void setSelectedAnswers(List<Answer> selectedAnswers) {
        this.selectedAnswers = selectedAnswers;
    }

    public String getFreeTextAnswer() {
        return freeTextAnswer;
    }

    public void setFreeTextAnswer(String freeTextAnswer) {
        this.freeTextAnswer = freeTextAnswer;
    }

    public Boolean getCorrect() {
        return isCorrect;
    }

    public void setCorrect(Boolean correct) {
        isCorrect = correct;
    }
}
