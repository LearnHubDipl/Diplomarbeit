package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "streak_tracking")
public class StreakTracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "last_logged_in")
    private LocalDate lastLoggedInAt;
    private Integer streak;


    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"streakTracking"})
    private User user;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getLastLoggedInAt() {
        return lastLoggedInAt;
    }

    public void setLastLoggedInAt(LocalDate lastLoggedInAt) {
        this.lastLoggedInAt = lastLoggedInAt;
    }

    public Integer getStreak() {
        return streak;
    }

    public void setStreak(Integer streak) {
        this.streak = streak;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
