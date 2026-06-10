package com.findapi.api.pricing.dto.request;

import java.math.BigDecimal;

import com.findapi.api.enums.BillingType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class PricingPlanCreateRequest {
    @NotBlank
    @Size(max = 120)
    private String name;

    @NotNull
    private BillingType billingType;

    @Size(max = 120)
    private String freeLimit;

    @DecimalMin(value = "0.00")
    private BigDecimal price;

    @Size(min = 3, max = 3)
    @Pattern(regexp = "^[A-Za-z]{3}$")
    private String currency;

    @Size(max = 5000)
    private String description;
}
