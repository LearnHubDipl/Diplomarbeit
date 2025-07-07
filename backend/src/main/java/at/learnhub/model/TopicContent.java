package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "topic_content")
public class TopicContent {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "is_approved")
    private Boolean isApproved;
    private LocalDate date;



    @ManyToOne()
    @JoinColumn(name = "media_id")
    private MediaFile media;

    @ManyToOne
    @JoinColumn(name = "topic_pool_id")
    @JsonIgnoreProperties({"topicContents"})
    private TopicPool topicPool;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"ownedTopicContents"})
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    @JsonIgnoreProperties({"approvedTopicContents"})
    private User approvedBy;

    @ManyToOne
    @JoinColumn(name = "teacher")
    @JsonIgnoreProperties({"teacherOfTopicContents"})
    private User taughtBy;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getApproved() {
        return isApproved;
    }

    public void setApproved(Boolean approved) {
        isApproved = approved;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public MediaFile getMedia() {
        return media;
    }

    public void setMedia(MediaFile media) {
        this.media = media;
    }

    public TopicPool getTopicPool() {
        return topicPool;
    }

    public void setTopicPool(TopicPool topicPool) {
        this.topicPool = topicPool;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public User getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(User approvedBy) {
        this.approvedBy = approvedBy;
    }

    public User getTaughtBy() {
        return taughtBy;
    }

    public void setTaughtBy(User taughtBy) {
        this.taughtBy = taughtBy;
    }
}
