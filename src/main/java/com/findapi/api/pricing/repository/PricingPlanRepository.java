package com.findapi.api.pricing.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.findapi.api.entity.PricingPlanEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PricingPlanRepository extends JpaRepository<PricingPlanEntity, UUID>,
        JpaSpecificationExecutor<PricingPlanEntity> {
    Optional<PricingPlanEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<PricingPlanEntity> findByApiIdAndDeletedAtIsNull(UUID apiId);

    boolean existsByApiIdAndNameIgnoreCaseAndDeletedAtIsNull(UUID apiId, String name);

    boolean existsByApiIdAndNameIgnoreCaseAndIdNotAndDeletedAtIsNull(UUID apiId, String name, UUID id);
}
