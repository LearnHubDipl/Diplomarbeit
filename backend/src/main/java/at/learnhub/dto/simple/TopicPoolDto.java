package at.learnhub.dto.simple;


import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "DTO of the TopicPool entity, containing basic information about a topic pool and their relations.")
public record TopicPoolDto(

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
        String description,

        @Schema(
                description = "Subject this topic pool belongs to.",
                implementation = SubjectSlimDto.class
        )
        SubjectSlimDto subject

        // TODO: other relations

) {}
