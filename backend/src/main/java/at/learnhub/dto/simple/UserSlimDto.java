package at.learnhub.dto.simple;

import at.learnhub.model.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

public record UserSlimDto(
        Long id,
        String name,
        String email,
        Boolean isTeacher,
        Boolean isAdmin,
        MediaFile profilePicture
) {
}