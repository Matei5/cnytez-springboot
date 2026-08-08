package cnytez.reddit.app.mapper;

import cnytez.reddit.app.dto.AuthResponse;
import cnytez.reddit.app.dto.AuthUserDto;
import cnytez.reddit.app.dto.UserProfileDto;
import cnytez.reddit.app.model.User;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface AuthMapper {
    AuthUserDto toAuthUserDto(User user);

    @Mapping(target = "accessToken", source = "token")
    @Mapping(target = "user", source = "user")
    AuthResponse toAuthResponse(User user, String token);

    @Mapping(target = "displayName", source = "user.name")
    @Mapping(target = "avatarUrl", source = "user.profilePhoto")
    UserProfileDto toProfileDto(User user);
}