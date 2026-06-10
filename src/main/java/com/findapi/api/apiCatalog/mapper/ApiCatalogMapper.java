package com.findapi.api.apiCatalog.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.findapi.api.apiCatalog.dto.response.ApiDetailResponse;
import com.findapi.api.apiCatalog.dto.response.ApiResponse;
import com.findapi.api.entity.ApiEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ApiCatalogMapper {
    @Mapping(target = "authenticationMethodId", source = "authenticationMethod.id")
    ApiResponse toResponse(ApiEntity entity);

    @Mapping(target = "authenticationMethodId", source = "authenticationMethod.id")
    @Mapping(target = "authenticationMethodName", source = "authenticationMethod.name")
    @Mapping(target = "ratingAverage", ignore = true)
    @Mapping(target = "ratingCount", ignore = true)
    @Mapping(target = "oneStar", ignore = true)
    @Mapping(target = "twoStars", ignore = true)
    @Mapping(target = "threeStars", ignore = true)
    @Mapping(target = "fourStars", ignore = true)
    @Mapping(target = "fiveStars", ignore = true)
    ApiDetailResponse toDetailResponse(ApiEntity entity);

    default Instant map(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }
}
