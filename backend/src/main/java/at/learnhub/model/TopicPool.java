package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "topic_pool")
public class TopicPool {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;


    @ManyToOne
    @JoinColumn(name = "subject_id")
    @JsonIgnoreProperties({"topicPools"})
    private Subject subject;


    @OneToMany(mappedBy = "topicPool")
    @JsonIgnoreProperties({"topicPool"})
    private List<TopicContent> topicContents;


    @OneToMany(mappedBy = "topicPool")
    @JsonIgnoreProperties({"topicPool"})
    private List<Question> questions;


    @ManyToMany(mappedBy = "topicPools")
    @JsonIgnoreProperties({"topicPools"})
    private List<Exam> exams;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public List<TopicContent> getTopicContents() {
        return topicContents;
    }

    public void setTopicContents(List<TopicContent> topicContents) {
        this.topicContents = topicContents;
    }

    public List<Exam> getExams() {
        return exams;
    }

    public void setExams(List<Exam> exams) {
        this.exams = exams;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
