package com.findapi.api.review.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.findapi.api.entity.ReviewEntity;
import com.findapi.api.review.dto.response.ReviewDetailResponse;
import com.findapi.api.review.dto.response.ReviewResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(target = "apiId", source = "api.id")
    @Mapping(target = "userId", source = "user.id")
    ReviewResponse entityToResponse(ReviewEntity entity);

    @Mapping(target = "apiId", source = "api.id")
    @Mapping(target = "userId", source = "user.id")
    ReviewDetailResponse entityToDetailResponse(ReviewEntity entity);

    default Integer map(Short value) {
        return value == null ? null : value.intValue();
    }

    default Instant map(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }
}
