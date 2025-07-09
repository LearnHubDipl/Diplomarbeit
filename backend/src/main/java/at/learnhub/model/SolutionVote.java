package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "solution_vote")
public class SolutionVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "is_up_vote")
    private Boolean isUpVote;


    @ManyToOne
    @JoinColumn(name = "solution_id")
    @JsonIgnoreProperties({"votes", "user"})
    private Solution solution;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"solutionVotes", "solutions", "questionPool", "createdQuestions"})
    private User user;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getUpVote() {
        return isUpVote;
    }

    public void setUpVote(Boolean upVote) {
        isUpVote = upVote;
    }

    public Solution getSolution() {
        return solution;
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
