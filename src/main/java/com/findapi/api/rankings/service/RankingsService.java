package com.findapi.api.rankings.service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.findapi.api.apiCatalog.mapper.ApiCatalogMapper;
import com.findapi.api.apiCatalog.repository.ApiRepository;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.rankings.dto.response.RankingApiResponse;
import com.findapi.api.review.repository.ReviewRepository;
import com.findapi.api.review.repository.ReviewRatingSummary;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankingsService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ApiRepository apiRepository;
    private final ReviewRepository reviewRepository;
    private final ApiCatalogMapper apiCatalogMapper;

    @Cacheable(cacheNames = "rankings", key = "'top-rated:' + T(java.util.Objects).toString(#pageable)")
    @Transactional(readOnly = true)
    public PageResponse<RankingApiResponse> topRated(Pageable pageable) {
        Pageable safePageable = safePageable(pageable, Sort.unsorted());
        return toResponse(apiRepository.findTopRated(safePageable));
    }

    @Cacheable(cacheNames = "rankings", key = "'free:' + T(java.util.Objects).toString(#pageable)")
    @Transactional(readOnly = true)
    public PageResponse<RankingApiResponse> free(Pageable pageable) {
        return toResponse(apiRepository.findByFreeTierTrueAndDeletedAtIsNull(safePageable(pageable, newest())));
    }

    @Cacheable(cacheNames = "rankings", key = "'open-source:' + T(java.util.Objects).toString(#pageable)")
    @Transactional(readOnly = true)
    public PageResponse<RankingApiResponse> openSource(Pageable pageable) {
        return toResponse(apiRepository.findByOpenSourceTrueAndDeletedAtIsNull(safePageable(pageable, newest())));
    }

    @Cacheable(cacheNames = "rankings", key = "'brazilian:' + T(java.util.Objects).toString(#pageable)")
    @Transactional(readOnly = true)
    public PageResponse<RankingApiResponse> brazilian(Pageable pageable) {
        return toResponse(apiRepository.findByBrazilianTrueAndDeletedAtIsNull(safePageable(pageable, newest())));
    }

    @Cacheable(cacheNames = "rankings", key = "'newest:' + T(java.util.Objects).toString(#pageable)")
    @Transactional(readOnly = true)
    public PageResponse<RankingApiResponse> newest(Pageable pageable) {
        return toResponse(apiRepository.findByDeletedAtIsNull(safePageable(pageable, newest())));
    }

    private PageResponse<RankingApiResponse> toResponse(Page<ApiEntity> page) {
        Map<UUID, ReviewRatingSummary> summaries = ratingSummaries(page);
        return PageResponse.from(page.map(entity -> toResponse(entity, summaries.get(entity.getId()))));
    }

    private RankingApiResponse toResponse(ApiEntity entity, ReviewRatingSummary summary) {
        Double average = summary == null ? null : summary.getRatingAverage();
        return RankingApiResponse.builder()
                .api(apiCatalogMapper.toResponse(entity))
                .ratingAverage(average == null ? 0.0 : Math.round(average * 100.0) / 100.0)
                .ratingCount(summary == null ? 0 : summary.getRatingCount())
                .build();
    }

    private Map<UUID, ReviewRatingSummary> ratingSummaries(Page<ApiEntity> page) {
        if (page.isEmpty()) {
            return Collections.emptyMap();
        }
        var apiIds = page.getContent().stream().map(ApiEntity::getId).toList();
        return reviewRepository.summarizeRatings(apiIds).stream()
                .collect(Collectors.toMap(ReviewRatingSummary::getApiId, Function.identity()));
    }

    private Pageable safePageable(Pageable pageable, Sort defaultSort) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, DEFAULT_PAGE_SIZE, defaultSort);
        }
        int page = Math.max(pageable.getPageNumber(), 0);
        int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : defaultSort;
        return PageRequest.of(page, size, sort);
    }

    private Sort newest() {
        return Sort.by(Sort.Direction.DESC, "createdAt");
    }
}
