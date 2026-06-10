package com.findapi.api.review.dto.response;

import java.time.Instant;
import java.util.UUID;

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
public class ReviewResponse {
    private UUID id;
    private UUID apiId;
    private UUID userId;
    private Integer rating;
    private String comment;
    private Instant createdAt;
    private Instant updatedAt;
}
