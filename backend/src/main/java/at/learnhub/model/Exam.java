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
    @JsonIgnoreProperties({"exams"})
    private User user;

    @ManyToMany
    @JoinTable(
            name = "exam_topic_pool",
            joinColumns = @JoinColumn(name = "exam_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_pool_id")
    )
    @JsonIgnoreProperties({"exams"})
    private List<TopicPool> topicPools;
}
