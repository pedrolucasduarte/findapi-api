package com.findapi.api.authenticationMethod.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.findapi.api.authenticationMethod.dto.response.AuthenticationMethodDetailResponse;
import com.findapi.api.authenticationMethod.dto.response.AuthenticationMethodResponse;
import com.findapi.api.entity.AuthenticationMethodEntity;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthenticationMethodMapper {
    AuthenticationMethodResponse entityToResponse(AuthenticationMethodEntity entity);

    AuthenticationMethodDetailResponse entityToDetailResponse(AuthenticationMethodEntity entity);

    default Instant map(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }
}
