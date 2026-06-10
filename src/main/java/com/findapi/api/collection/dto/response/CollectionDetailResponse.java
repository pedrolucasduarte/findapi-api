package com.findapi.api.collection.dto.response;

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
public class CollectionDetailResponse {
    private UUID id;
    private UUID ownerId;
    private String name;
    private String slug;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}
