package at.learnhub.mapper;

import at.learnhub.dto.request.UserCreateDto;
import at.learnhub.dto.simple.UserSlimDto;
import at.learnhub.model.MediaFile;
import at.learnhub.model.User;

/**
 * Utility class for mapping {@link User} entities to slim Dtos.
 */
public class UserMapper {
    /**
     * Maps a User entity to a UserSlimDto.
     * @param user the User entity
     * @return the UserSlimDto
     */
    public static UserSlimDto toSlimDto(User user) {
        if (user == null) return null;

        return new UserSlimDto(
                user.getId(),
                user.getKeycloakSub(),
                user.getName(),
                user.getEmail(),
                user.getTeacher(),
                user.getAdmin(),
                user.getProfilePicture(),
                user.getClassName()
        );
    }

    /**
     * Maps a UserCreateDto to a User entity.
     * @param dto the UserCreateDto
     * @return the User entity
     */
    public static User toEntity(UserCreateDto dto) {
        if (dto == null) return null;

        User user = new User();
        user.setKeycloakSub(dto.keycloakSub());
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setUsername(dto.username());
        user.setGivenName(dto.givenName());
        user.setFamilyName(dto.familyName());
        user.setClassName(dto.className());
        user.setTeacher(dto.isTeacher() != null ? dto.isTeacher() : false);
        user.setAdmin(false);

        return user;
    }

    public static Long mediaId(MediaFile m) {
        return m != null ? m.getId() : null;
    }
}