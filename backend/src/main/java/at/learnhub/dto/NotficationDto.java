package at.learnhub.dto;

import java.util.Map;

public class NotficationDto {
    public String id;
    public String type;
    public String title;
    public String message;
    public long createdAt;
    public boolean read;

    public Map<String, Object> meta;

    public NotficationDto() {}
}