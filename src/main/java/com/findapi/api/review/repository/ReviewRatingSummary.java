package com.findapi.api.review.repository;

import java.util.UUID;

public interface ReviewRatingSummary {
    UUID getApiId();

    Double getRatingAverage();

    long getRatingCount();
}
