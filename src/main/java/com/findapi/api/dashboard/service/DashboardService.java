package com.findapi.api.dashboard.service;

import java.util.List;

import com.findapi.api.apiCatalog.dto.response.ApiResponse;
import com.findapi.api.apiCatalog.mapper.ApiCatalogMapper;
import com.findapi.api.apiCatalog.repository.ApiRepository;
import com.findapi.api.category.repository.CategoryRepository;
import com.findapi.api.collection.repository.CollectionRepository;
import com.findapi.api.dashboard.dto.response.DashboardResponse;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.review.repository.ReviewRepository;
import com.findapi.api.tag.repository.TagRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final int HIGHLIGHT_LIMIT = 5;

    private final ApiRepository apiRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ReviewRepository reviewRepository;
    private final CollectionRepository collectionRepository;
    private final ApiCatalogMapper apiCatalogMapper;

    @Cacheable("dashboard")
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        PageRequest newest = PageRequest.of(
                0,
                HIGHLIGHT_LIMIT,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return DashboardResponse.builder()
                .totalApis(apiRepository.countByDeletedAtIsNull())
                .totalCategories(categoryRepository.countByDeletedAtIsNull())
                .totalTags(tagRepository.countByDeletedAtIsNull())
                .totalReviews(reviewRepository.countByDeletedAtIsNull())
                .totalCollections(collectionRepository.countByDeletedAtIsNull())
                .latestApis(toResponses(apiRepository.findByDeletedAtIsNull(newest)))
                .topRatedApis(toResponses(apiRepository.findTopRated(PageRequest.of(0, HIGHLIGHT_LIMIT))))
                .brazilianApis(toResponses(apiRepository.findByBrazilianTrueAndDeletedAtIsNull(newest)))
                .build();
    }

    private List<ApiResponse> toResponses(Page<ApiEntity> page) {
        return page.getContent().stream().map(apiCatalogMapper::toResponse).toList();
    }
}
