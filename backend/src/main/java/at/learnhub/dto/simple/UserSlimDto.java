package at.learnhub.dto.simple;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "DTO containing the minimal user information")
public record UserSlimDto(
        @Schema(
                description = "Unique identifier for the user.",
                example = "4"
        )
        Long id,

        @Schema(
                description = "Keycloak sub (unique identifier from Keycloak).",
                example = "cbd71d68-661c-4fd7-bced-3444f968d59d"
        )
        String keycloakSub,

        @Schema(
                description = "The name of the user.",
                example = "Isabella Baumann"
        )
        String name,

        @Schema(
                description = "The email address of the user",
                example = "i.baumann@students.htl-leonding.ac.at"
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
                implementation = MediaFileSlimDto.class
        )
        MediaFileSlimDto profilePicture,

        @Schema(
                description = "Class name of the user (e.g., 5AHITM).",
                example = "5AHITM"
        )
        String className,

        @Schema(
                description = "Username in keycloak",
                example = "it210181"
        )
        String username
) {
}