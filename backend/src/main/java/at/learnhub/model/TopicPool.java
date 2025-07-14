package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

/**
 * A pool of topics within a subject.
 */
@Entity
@Table(name = "topic_pool")
public class TopicPool {

    /**
     * Unique identifier of the topic pool.
     * Example: 10
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the topic pool.
     * Example: Algebra Basics
     */
    private String name;

    /**
     * Description or summary of the topic pool.
     * Example: This topic pool covers the basics of algebra including variables and equations.
     */
    private String description;

    /**
     * Subject this topic pool belongs to.
     */
    @ManyToOne
    @JoinColumn(name = "subject_id")
    @JsonIgnoreProperties({"topicPools"})
    private Subject subject;

    /**
     * List of topic contents belonging to this topic pool.
     */
    @OneToMany(mappedBy = "topicPool")
    @JsonIgnoreProperties({"topicPool"})
    private List<TopicContent> topicContents;

    /**
     * List of questions belonging to this topic pool.
     */
    @OneToMany(mappedBy = "topicPool")
    @JsonIgnoreProperties({"topicPool"})
    private List<Question> questions;

    /**
     * Exams associated with this topic pool.
     */
    @ManyToMany(mappedBy = "topicPools")
    @JsonIgnoreProperties({"topicPools"})
    private List<Exam> exams;

    // Getter und Setter

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

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<Exam> getExams() {
        return exams;
    }

    public void setExams(List<Exam> exams) {
        this.exams = exams;
    }
}
