package cnytez.reddit.app.mapper;

import cnytez.reddit.app.dto.internal.UserDto;
import cnytez.reddit.app.model.User;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface UserMapper {
    @Mapping(target = "username", source = "user", qualifiedByName = "mapUsername")
    UserDto toDto(User user);

    @Named("mapUsername")
    default String mapUsername(User user) {
        return user.getDeletionDate() != null ? "[deleted]" : user.getUsername();
    }
}
