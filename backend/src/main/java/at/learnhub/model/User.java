package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;

/**
 * Represents a user in the LearnHub system with roles and associated content.
 */
@Entity
@Table(name = "\"user\"")
@NamedQueries({
        @NamedQuery(
                name = User.FIND_BY_KEYCLOAK_SUB,
                query = "SELECT u FROM User u WHERE u.keycloakSub = :sub"
        ),
        @NamedQuery(
                name = User.FIND_BY_EMAIL,
                query = "SELECT u FROM User u WHERE u.email = :email"
        )
})
public class User {
    public static final String FIND_BY_KEYCLOAK_SUB = "User.findByKeycloakSub";
    public static final String FIND_BY_EMAIL = "User.findByEmail";

    /**
     * Unique identifier of the user (auto-generated).
     * Example: 123
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Keycloak sub claim (unique identifier from Keycloak).
     * Example: "cbd71d68-661c-4fd7-bced-3444f968d59d"
     */
    @Column(name = "keycloak_sub", unique = true, nullable = false, length = 255)
    private String keycloakSub;

    /**
     * Full name of the user.
     * Example: Isabella Baumann
     */
    private String name;

    /**
     * Email address of the user.
     * Example: i.baumann@students.htl-leonding.ac.at
     */
    @Column(unique = true)
    private String email;

    /**
     * Indicates if the user is a teacher.
     * Example: false
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
     * Username from Keycloak (preferred_username).
     * Example: "it210181"
     */
    @Column(name = "username")
    private String username;

    /**
     * Given name from Keycloak.
     * Example: "Isabella"
     */
    @Column(name = "given_name")
    private String givenName;

    /**
     * Family name from Keycloak.
     * Example: "Baumann"
     */
    @Column(name = "family_name")
    private String familyName;

    /**
     * Class from distinguished name.
     * Example: "5AHITM"
     */
    @Column(name = "class_name")
    private String className;

    /**
     * Profile picture associated with the user.
     */
    @ManyToOne
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
    @OneToOne(mappedBy = "user", cascade = CascadeType.PERSIST)
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

    // Constructors
    public User() {
    }

    public User(String keycloakSub, String name, String email, Boolean isTeacher) {
        this.keycloakSub = keycloakSub;
        this.name = name;
        this.email = email;
        this.isTeacher = isTeacher;
        this.isAdmin = false;
    }

    // Getter und Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeycloakSub() {
        return keycloakSub;
    }

    public void setKeycloakSub(String keycloakSub) {
        this.keycloakSub = keycloakSub;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
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