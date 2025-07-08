package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

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

    @OneToMany(mappedBy = "questionPool")
    @JsonIgnoreProperties({"questionPool"})
    private List<QuestionPoolEntry> entries;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<QuestionPoolEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<QuestionPoolEntry> entries) {
        this.entries = entries;
    }
}
