package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "exam_question")
public class ExamQuestion {

    @EmbeddedId
    private ExamQuestionId id;

    @ManyToOne
    @MapsId("examId")
    @JoinColumn(name = "exam_id")
    @JsonIgnoreProperties("questions")
    private Exam exam;

    @ManyToOne
    @MapsId("entryId")
    @JoinColumns({
            @JoinColumn(name = "question_id", referencedColumnName = "question_id"),
            @JoinColumn(name = "pool_id", referencedColumnName = "pool_id")
    })
    @JsonIgnoreProperties("examQuestions")
    private QuestionPoolEntry entry;


    /*
    If Question Type is MultipleChoice, then this field will contain all given answers
     */
    @ManyToMany
    @JoinTable(
            name = "exam_question_selected_answers",
            joinColumns = {
                    @JoinColumn(name = "exam_id", referencedColumnName = "exam_id"),
                    @JoinColumn(name = "question_id", referencedColumnName = "question_id"),
                    @JoinColumn(name = "pool_id", referencedColumnName = "pool_id")
            },
            inverseJoinColumns = @JoinColumn(name = "answer_id")
    )
    @JsonIgnoreProperties("examQuestions")
    private List<Answer> selectedAnswers;


    /*
    If Question Type is freeText, then this field will contain the given text answer
     */
    @Column(name = "freet_text_answer")
    private String freeTextAnswer;
    @Column(name = "is_correct")
    private Boolean isCorrect;


    public ExamQuestionId getId() {
        return id;
    }

    public void setId(ExamQuestionId id) {
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
