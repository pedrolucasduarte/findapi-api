package com.findapi.api.rankings.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import com.findapi.api.apiCatalog.dto.response.ApiResponse;
import com.findapi.api.apiCatalog.mapper.ApiCatalogMapper;
import com.findapi.api.apiCatalog.repository.ApiRepository;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.review.repository.ReviewRepository;
import com.findapi.api.review.repository.ReviewRatingSummary;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class RankingsServiceTest {
    @Mock private ApiRepository apiRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private ApiCatalogMapper apiCatalogMapper;

    @Test
    void returnTopRatedApisWithReviewStats() {
        UUID apiId = UUID.randomUUID();
        ApiEntity entity = new ApiEntity();
        entity.setId(apiId);
        when(apiRepository.findTopRated(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(apiCatalogMapper.toResponse(entity)).thenReturn(ApiResponse.builder().id(apiId).build());
        ReviewRatingSummary summary = new ReviewRatingSummary() {
            public UUID getApiId() {
                return apiId;
            }

            public Double getRatingAverage() {
                return 4.666;
            }

            public long getRatingCount() {
                return 12;
            }
        };
        when(reviewRepository.summarizeRatings(List.of(apiId))).thenReturn(List.of(summary));
        RankingsService service = new RankingsService(apiRepository, reviewRepository, apiCatalogMapper);

        var response = service.topRated(PageRequest.of(0, 20));

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).getRatingAverage()).isEqualTo(4.67);
        assertThat(response.content().get(0).getRatingCount()).isEqualTo(12);
    }
}
