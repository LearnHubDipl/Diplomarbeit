package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Content associated with a topic, including media and approval status.
 */
@Entity
@Table(name = "topic_content")
public class TopicContent {

    /**
     * Unique identifier of the topic content.
     * Example: 42
     */
    @Id
    @GeneratedValue
    private Long id;

    /**
     * Indicates whether the content has been approved.
     * Example: true
     */
    @Column(name = "is_approved")
    private Boolean isApproved;

    /**
     * Date the content was created.
     * Example: 2025-07-10
     */
    private LocalDate date;

    /**
     * Associated media file.
     */
    @ManyToOne()
    @JoinColumn(name = "media_id")
    private MediaFile media;

    /**
     * The topic pool this content belongs to.
     */
    @ManyToOne
    @JoinColumn(name = "topic_pool_id")
    @JsonIgnoreProperties({"topicContents", "user", "questions"})
    private TopicPool topicPool;

    /**
     * User who created the content.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"ownedTopicContents", "approvedTopicContents", "teacherOfTopicContents"})
    private User createdBy;

    /**
     * User who approved the content.
     */
    @ManyToOne
    @JoinColumn(name = "approved_by")
    @JsonIgnoreProperties({"ownedTopicContents", "approvedTopicContents", "teacherOfTopicContents"})
    private User approvedBy;

    /**
     * User who teaches or is responsible for this content.
     */
    @ManyToOne
    @JoinColumn(name = "teacher")
    @JsonIgnoreProperties({"ownedTopicContents", "approvedTopicContents", "teacherOfTopicContents"})
    private User taughtBy;

    // Getter und Setter

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
