package com.findapi.api.category.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.findapi.api.category.dto.response.CategoryDetailResponse;
import com.findapi.api.category.dto.response.CategoryResponse;
import com.findapi.api.entity.CategoryEntity;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponse entityToResponse(CategoryEntity entity);

    CategoryDetailResponse entityToDetailResponse(CategoryEntity entity);

    default Instant map(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }
}
