package at.learnhub.dto.simple;

import at.learnhub.model.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Schema(description = "DTO containing the minimal user information")
public record UserSlimDto(
        @Schema(
                description = "Unique identifier for the user.",
                example = "4"
        )
        Long id,

        @Schema(
                description = "The name of the user.",
                example = "Max Mustermann"
        )
        String name,

        @Schema(
                description = "The email adress of the user",
                example = "m.mustermann@students.htl-leonding.ac.at"
        )
        String email,

        @Schema(
                description = "Shows if the user is a teacher.",
                example = "false"
        )
        Boolean isTeacher,

        @Schema(
                description = "Whether the user has admin rights.",
                example = "false"
        )
        Boolean isAdmin,

        @Schema(
                description = "Profile picture of the user.",
                implementation = MediaFile.class
        )
        MediaFile profilePicture
) {
}