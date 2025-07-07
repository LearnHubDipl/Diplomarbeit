package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.List;


@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;

    @Column(name = "is_teacher")
    private Boolean isTeacher;
    @Column(name = "is_admin")
    private Boolean isAdmin;


    @ManyToOne()
    @JoinColumn(name = "profile_picture_id")
    private MediaFile profilePicture;

    @OneToMany(mappedBy = "createdBy")
    @JsonIgnoreProperties({"createdBy"})
    private List<TopicContent> ownedTopicContents;

    @OneToMany(mappedBy = "approvedBy")
    @JsonIgnoreProperties({"approvedBy"})
    private List<TopicContent> approvedTopicContents;

    @OneToMany(mappedBy = "teacher")
    @JsonIgnoreProperties({"teacher"})
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



    // private List<Question> createdQuestions;

    // private List<SolutionVote> solutionVotes;

}
