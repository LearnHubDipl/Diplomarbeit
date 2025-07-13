package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

/**
 * Represents a user in the LearnHub system with roles and associated content.
 */
@Entity
@Table(name = "\"user\"")
public class User {

    /**
     * Unique identifier of the user.
     * Example: 123
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Full name of the user.
     * Example: Max Mustermann
     */
    private String name;

    /**
     * Email address of the user.
     * Example: max@example.com
     */
    private String email;

    /**
     * Indicates if the user is a teacher.
     * Example: true
     */
    @Column(name = "is_teacher")
    private Boolean isTeacher;

    /**
     * Indicates if the user has admin privileges.
     * Example: false
     */
    @Column(name = "is_admin")
    private Boolean isAdmin;

    /**
     * Profile picture associated with the user.
     */
    @ManyToOne()
    @JoinColumn(name = "profile_picture_id")
    private MediaFile profilePicture;

    /**
     * Topic contents created by this user.
     */
    @OneToMany(mappedBy = "createdBy")
    @JsonIgnoreProperties({"createdBy", "approvedBy", "taughtBy"})
    private List<TopicContent> ownedTopicContents;

    /**
     * Topic contents approved by this user.
     */
    @OneToMany(mappedBy = "approvedBy")
    @JsonIgnoreProperties({"createdBy", "approvedBy", "taughtBy"})
    private List<TopicContent> approvedTopicContents;

    /**
     * Topic contents taught by this user.
     */
    @OneToMany(mappedBy = "taughtBy")
    @JsonIgnoreProperties({"createdBy", "approvedBy", "taughtBy"})
    private List<TopicContent> teacherOfTopicContents;

    /**
     * Streak tracking record of the user.
     */
    @OneToOne(mappedBy = "user")
    @JsonIgnoreProperties({"user"})
    private StreakTracking streakTracking;

    /**
     * Exams associated with the user.
     */
    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties({"user"})
    private List<Exam> exams;

    /**
     * Question pool associated with the user.
     */
    @OneToOne(mappedBy = "user")
    @JsonIgnoreProperties({"user"})
    private QuestionPool questionPool;

    /**
     * Questions created by the user.
     */
    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties({"user", "topicPool", "entries"})
    private List<Question> createdQuestions;

    /**
     * Solutions submitted by the user.
     */
    @OneToMany(mappedBy = "user")
    @JsonIgnoreProperties({"user"})
    private List<Solution> solutions;

    /**
     * Votes on solutions submitted by the user.
     */
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
