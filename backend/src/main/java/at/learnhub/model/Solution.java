package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Entity
@Schema(description = "Represents a solution submitted by a user for a specific question.")
public class Solution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique identifier of the solution, generated by the system.",
            example = "1001",
            readOnly = true
    )
    private Long id;

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonIgnoreProperties({"solutions", "user"})
    private Question question;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"solutions", "createdQuestions", "questionPool", "exams",
            "solutionVotes", "ownedTopicContents", "approvedTopicContents", "teacherOfTopicContents"})
    private User user;

    @OneToMany(mappedBy = "solution")
    @JsonIgnoreProperties({"solution"})
    private List<SolutionStep> steps;

    @OneToMany(mappedBy = "solution")
    @JsonIgnoreProperties({"solution"})
    private List<SolutionVote> votes;

    // Getters and setters

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
