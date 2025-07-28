package at.learnhub.mapper;

import at.learnhub.dto.simple.UserSlimDto;
import at.learnhub.model.User;

public class UserMapper {
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
}