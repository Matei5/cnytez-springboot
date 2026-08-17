package com.cnytez.app.mapper;

import com.cnytez.app.dto.response.AuthResponse;
import com.cnytez.app.dto.response.AuthUserDto;
import com.cnytez.app.dto.internal.UserProfileDto;
import com.cnytez.app.model.User;
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
    @Mapping(target = "avatarUrl", source = "user.profilePhotoUrl")
    UserProfileDto toProfileDto(User user);
}