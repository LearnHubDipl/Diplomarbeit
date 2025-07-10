package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Entity
@Table(name = "\"user\"")
@Schema(description = "Represents a user in the LearnHub system with roles and associated content")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(
            description = "Unique identifier of the user",
            example = "123",
            readOnly = true
    )
    private Long id;

    @Schema(
            description = "Full name of the user",
            example = "Max Mustermann"
    )
    private String name;

    @Schema(
            description = "Email address of the user",
            example = "max@example.com"
    )
    private String email;

    @Column(name = "is_teacher")
    @Schema(
            description = "Indicates if the user is a teacher",
            example = "true"
    )
    private Boolean isTeacher;

    @Column(name = "is_admin")
    @Schema(
            description = "Indicates if the user has admin privileges",
            example = "false"
    )
    private Boolean isAdmin;

    @ManyToOne()
    @JoinColumn(name = "profile_picture_id")
    @Schema(
            description = "Profile picture associated with the user",
            implementation = MediaFile.class
    )
    private MediaFile profilePicture;

    @OneToMany(mappedBy = "createdBy")
    @JsonIgnoreProperties({"createdBy", "approvedBy", "taughtBy"})
    private List<TopicContent> ownedTopicContents;

    @OneToMany(mappedBy = "approvedBy")
    @JsonIgnoreProperties({"createdBy", "approvedBy", "taughtBy"})
    private List<TopicContent> approvedTopicContents;

    @OneToMany(mappedBy = "taughtBy")
    @JsonIgnoreProperties({"createdBy", "approvedBy", "taughtBy"})
    private List<TopicContent> teacherOfTopicContents;

    @OneToOne(mappedBy = "user")
    @JsonIgnoreProperties({"user"})
    private StreakTracking streakTracking;

    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties({"user"})
    private List<Exam> exams;

    @OneToOne(mappedBy = "user")
    @JsonIgnoreProperties({"user"})
    private QuestionPool questionPool;

    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties({"user", "topicPool", "entries"})
    private List<Question> createdQuestions;

    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties({"user"})
    private List<Solution> solutions;

    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties({"user"})
    private List<SolutionVote> solutionVotes;

    // Getter und Setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getTeacher() {
        return isTeacher;
    }

    public void setTeacher(Boolean teacher) {
        isTeacher = teacher;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public MediaFile getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(MediaFile profilePicture) {
        this.profilePicture = profilePicture;
    }

    public List<TopicContent> getOwnedTopicContents() {
        return ownedTopicContents;
    }

    public void setOwnedTopicContents(List<TopicContent> ownedTopicContents) {
        this.ownedTopicContents = ownedTopicContents;
    }

    public List<TopicContent> getApprovedTopicContents() {
        return approvedTopicContents;
    }

    public void setApprovedTopicContents(List<TopicContent> approvedTopicContents) {
        this.approvedTopicContents = approvedTopicContents;
    }

    public List<TopicContent> getTeacherOfTopicContents() {
        return teacherOfTopicContents;
    }

    public void setTeacherOfTopicContents(List<TopicContent> teacherOfTopicContents) {
        this.teacherOfTopicContents = teacherOfTopicContents;
    }

    public StreakTracking getStreakTracking() {
        return streakTracking;
    }

    public void setStreakTracking(StreakTracking streakTracking) {
        this.streakTracking = streakTracking;
    }

    public List<Exam> getExams() {
        return exams;
    }

    public void setExams(List<Exam> exams) {
        this.exams = exams;
    }

    public QuestionPool getQuestionPool() {
        return questionPool;
    }

    public void setQuestionPool(QuestionPool questionPool) {
        this.questionPool = questionPool;
    }

    public List<Question> getCreatedQuestions() {
        return createdQuestions;
    }

    public void setCreatedQuestions(List<Question> createdQuestions) {
        this.createdQuestions = createdQuestions;
    }

    public List<Solution> getSolutions() {
        return solutions;
    }

    public void setSolutions(List<Solution> solutions) {
        this.solutions = solutions;
    }

    public List<SolutionVote> getSolutionVotes() {
        return solutionVotes;
    }

    public void setSolutionVotes(List<SolutionVote> solutionVotes) {
        this.solutionVotes = solutionVotes;
    }
}
