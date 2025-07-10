package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String text;
    private String explanation;
    private Integer type;
    private Integer difficulty;
    @Column(name = "is_public")
    private Boolean isPublic;


    @ManyToOne
    @JoinColumn(name = "topic_pool_id")
    @JsonIgnoreProperties({"questions", "topicContents", "exams"})
    private TopicPool topicPool;


    @ManyToOne
    @JoinColumn(name = "media_id")
    private MediaFile media;


    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"createdQuestions", "topicContents"})
    private User user;


    @OneToMany(mappedBy = "question")
    @JsonIgnoreProperties({"question"})
    private List<Answer> answers;


    @OneToMany(mappedBy = "question")
    @JsonIgnoreProperties({"question"})
    private List<QuestionPoolEntry> entries;


    @OneToMany(mappedBy = "question")
    @JsonIgnoreProperties({"question"})
    private List<Solution> solutions;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public TopicPool getTopicPool() {
        return topicPool;
    }

    public void setTopicPool(TopicPool topicPool) {
        this.topicPool = topicPool;
    }

    public MediaFile getMedia() {
        return media;
    }

    public void setMedia(MediaFile media) {
        this.media = media;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public List<QuestionPoolEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<QuestionPoolEntry> entries) {
        this.entries = entries;
    }

    public List<Solution> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<Solution> solutions) {
        this.solutions = solutions;
    }

    public Boolean getPublic() {
        return isPublic;
    }

    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
    }
}
