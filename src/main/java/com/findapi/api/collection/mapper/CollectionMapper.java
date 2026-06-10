package com.findapi.api.collection.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.findapi.api.collection.dto.response.CollectionDetailResponse;
import com.findapi.api.collection.dto.response.CollectionResponse;
import com.findapi.api.entity.CollectionEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CollectionMapper {
    @Mapping(target = "ownerId", source = "user.id")
    CollectionResponse entityToResponse(CollectionEntity entity);

    @Mapping(target = "ownerId", source = "user.id")
    CollectionDetailResponse entityToDetailResponse(CollectionEntity entity);

    default Instant map(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }
}
