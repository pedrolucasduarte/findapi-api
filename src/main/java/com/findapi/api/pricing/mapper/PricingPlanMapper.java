package com.findapi.api.pricing.mapper;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.findapi.api.entity.PricingPlanEntity;
import com.findapi.api.pricing.dto.response.PricingPlanDetailResponse;
import com.findapi.api.pricing.dto.response.PricingPlanResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PricingPlanMapper {
    @Mapping(target = "apiId", source = "api.id")
    PricingPlanResponse entityToResponse(PricingPlanEntity entity);

    @Mapping(target = "apiId", source = "api.id")
    PricingPlanDetailResponse entityToDetailResponse(PricingPlanEntity entity);

    default Instant map(OffsetDateTime value) {
        return value == null ? null : value.toInstant();
    }
}
