package at.learnhub.mapper;

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
                user.getName(),
                user.getEmail(),
                user.getTeacher(),
                user.getAdmin(),
                user.getProfilePicture()
        );
    }
    public static Long mediaId(MediaFile m) {
        return m != null ? m.getId() : null;
    }
}