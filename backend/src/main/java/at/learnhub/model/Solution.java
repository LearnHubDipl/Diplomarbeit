package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Solution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonIgnoreProperties({"solutions"})
    private Question question;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"solutions"})
    private User user;

    @OneToMany(mappedBy = "solution")
    @JsonIgnoreProperties({"solution"})
    private List<SolutionStep> steps;

    @OneToMany(mappedBy = "solution")
    @JsonIgnoreProperties({"solution"})
    private List<SolutionVote> votes;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<SolutionStep> getSteps() {
        return steps;
    }

    public void setSteps(List<SolutionStep> steps) {
        this.steps = steps;
    }

    public List<SolutionVote> getVotes() {
        return votes;
    }

    public void setVotes(List<SolutionVote> votes) {
        this.votes = votes;
    }
}
