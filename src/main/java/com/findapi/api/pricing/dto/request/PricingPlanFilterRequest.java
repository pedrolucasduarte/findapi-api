package com.findapi.api.pricing.dto.request;

import java.math.BigDecimal;
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
public class PricingPlanFilterRequest {
    private UUID apiId;
    private String name;
    private BillingType billingType;
    private String currency;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
