package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "time_limit")
    private Integer timeLimit;
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    private Integer questionCount;
    private Double score;


    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"exams", "ownedTopicContents", "approvedTopicContents", "teacherOfTopicContents"})
    private User user;

    @ManyToMany
    @JoinTable(
            name = "exam_topic_pool",
            joinColumns = @JoinColumn(name = "exam_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_pool_id")
    )
    @JsonIgnoreProperties({"exams", "topicContents", "questions"})
    private List<TopicPool> topicPools;

    @OneToMany(mappedBy = "exam")
    @JsonIgnoreProperties({"exam"})
    private List<ExamQuestion> questions;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Integer getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(Integer questionCount) {
        this.questionCount = questionCount;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<TopicPool> getTopicPools() {
        return topicPools;
    }

    public void setTopicPools(List<TopicPool> topicPools) {
        this.topicPools = topicPools;
    }

    public List<ExamQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<ExamQuestion> questions) {
        this.questions = questions;
    }
}
