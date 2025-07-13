package at.learnhub.dto.simple;


import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Slim version of the TopicPool DTO, containing basic information about a topic pool without relations.")
public record TopicPoolSlimDto(

        @Schema(
                description = "Unique identifier of the topic pool",
                example = "10",
                readOnly = true
        )
        Long id,

        @Schema(
                description = "Name of the topic pool",
                example = "Algebra Basics"
        )
        String name,

        @Schema(
                description = "Description or summary of the topic pool",
                example = "This topic pool covers the basics of algebra including variables and equations."
        )
        String description

) {}
