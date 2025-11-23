package at.learnhub.dto.request;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "DTO for creating a new user from Keycloak token")
public record UserCreateDto(
        @Schema(
                description = "Keycloak sub (unique user ID)",
                example = "cbd71d68-661c-4fd7-bced-3444f968d59d",
                required = true
        )
        String keycloakSub,

        @Schema(
                description = "Full name",
                example = "Isabella Baumann",
                required = true
        )
        String name,

        @Schema(
                description = "Email address",
                example = "i.baumann@students.htl-leonding.ac.at",
                required = true
        )
        String email,

        @Schema(
                description = "Keycloak username",
                example = "it210181"
        )
        String username,

        @Schema(
                description = "Given name",
                example = "Isabella"
        )
        String givenName,

        @Schema(
                description = "Family name",
                example = "Baumann"
        )
        String familyName,

        @Schema(
                description = "Class name extracted from DN",
                example = "5AHITM"
        )
        String className,

        @Schema(
                description = "Is the user a teacher?",
                example = "false"
        )
        Boolean isTeacher
) {
}