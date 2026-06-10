package com.findapi.api.pricing.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.findapi.api.enums.BillingType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingPlanResponse {
    private UUID id;
    private UUID apiId;
    private String name;
    private BillingType billingType;
    private String freeLimit;
    private BigDecimal price;
    private String currency;
    private Instant createdAt;
    private Instant updatedAt;
}
