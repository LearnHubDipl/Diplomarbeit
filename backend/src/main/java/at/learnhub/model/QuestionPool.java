package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "question_pool")
public class QuestionPool {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToOne()
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"user"})
    private User user;
}
