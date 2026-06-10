package com.findapi.api.search.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.findapi.api.entity.ApiEntity;
import com.findapi.api.search.dto.response.ApiSearchResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SearchMapper {
    @Mapping(target = "authenticationMethodId", source = "authenticationMethod.id")
    ApiSearchResponse toResponse(ApiEntity entity);

    default Instant map(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }
}
