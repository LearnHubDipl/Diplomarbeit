package at.learnhub.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;


    @ManyToOne()
    @JoinColumn(name = "media_id")
    private MediaFile img;


    @OneToMany(mappedBy = "subject")
    @JsonIgnoreProperties({"subject"})
    private List<TopicPool> topicPools;


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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MediaFile getImg() {
        return img;
    }

    public void setImg(MediaFile img) {
        this.img = img;
    }

    public List<TopicPool> getTopicPools() {
        return topicPools;
    }

    public void setTopicPools(List<TopicPool> topicPools) {
        this.topicPools = topicPools;
    }
}
