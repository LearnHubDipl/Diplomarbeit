package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.LocalDate;

@Entity
@Table(name = "topic_content")
@Schema(description = "Content associated with a topic, including media and approval status")
public class TopicContent {

    @Id
    @GeneratedValue
    @Schema(
            description = "Unique identifier of the topic content",
            example = "42",
            readOnly = true
    )
    private Long id;

    @Column(name = "is_approved")
    @Schema(
            description = "Indicates whether the content has been approved",
            example = "true"
    )
    private Boolean isApproved;

    @Schema(
            description = "Date the content was created",
            example = "2025-07-10"
    )
    private LocalDate date;

    @ManyToOne()
    @JoinColumn(name = "media_id")
    @Schema(
            description = "Associated media file",
            implementation = MediaFile.class
    )
    private MediaFile media;

    @ManyToOne
    @JoinColumn(name = "topic_pool_id")
    @JsonIgnoreProperties({"topicContents", "user", "questions"})
    private TopicPool topicPool;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"ownedTopicContents", "approvedTopicContents", "teacherOfTopicContents"})
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    @JsonIgnoreProperties({"ownedTopicContents", "approvedTopicContents", "teacherOfTopicContents"})
    private User approvedBy;

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
