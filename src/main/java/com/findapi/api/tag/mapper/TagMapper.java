package com.findapi.api.tag.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.findapi.api.entity.TagEntity;
import com.findapi.api.tag.dto.response.TagDetailResponse;
import com.findapi.api.tag.dto.response.TagResponse;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TagMapper {
    TagResponse entityToResponse(TagEntity entity);

    TagDetailResponse entityToDetailResponse(TagEntity entity);

    default Instant map(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }
}
