package com.findapi.api.user.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.findapi.api.entity.AppUserEntity;
import com.findapi.api.user.dto.response.UserResponse;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse entityToResponse(AppUserEntity entity);

    default Instant map(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }
}
