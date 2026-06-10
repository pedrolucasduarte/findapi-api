package com.findapi.api.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;

import com.findapi.api.apiCatalog.dto.response.ApiResponse;
import com.findapi.api.apiCatalog.mapper.ApiCatalogMapper;
import com.findapi.api.apiCatalog.repository.ApiRepository;
import com.findapi.api.category.repository.CategoryRepository;
import com.findapi.api.collection.repository.CollectionRepository;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.review.repository.ReviewRepository;
import com.findapi.api.tag.repository.TagRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {
    @Mock private ApiRepository apiRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TagRepository tagRepository;
    @Mock private ReviewRepository reviewRepository;
    @Mock private CollectionRepository collectionRepository;
    @Mock private ApiCatalogMapper apiCatalogMapper;

    @Test
    void buildDashboardFromActiveData() {
        ApiEntity entity = new ApiEntity();
        ApiResponse mapped = ApiResponse.builder().name("FindApi").build();
        PageImpl<ApiEntity> page = new PageImpl<>(List.of(entity));
        when(apiRepository.countByDeletedAtIsNull()).thenReturn(10L);
        when(categoryRepository.countByDeletedAtIsNull()).thenReturn(4L);
        when(tagRepository.countByDeletedAtIsNull()).thenReturn(8L);
        when(reviewRepository.countByDeletedAtIsNull()).thenReturn(20L);
        when(collectionRepository.countByDeletedAtIsNull()).thenReturn(3L);
        when(apiRepository.findByDeletedAtIsNull(any(Pageable.class))).thenReturn(page);
        when(apiRepository.findTopRated(any(Pageable.class))).thenReturn(page);
        when(apiRepository.findByBrazilianTrueAndDeletedAtIsNull(any(Pageable.class))).thenReturn(page);
        when(apiCatalogMapper.toResponse(entity)).thenReturn(mapped);
        DashboardService service = new DashboardService(
                apiRepository,
                categoryRepository,
                tagRepository,
                reviewRepository,
                collectionRepository,
                apiCatalogMapper
        );

        var response = service.getDashboard();

        assertThat(response.getTotalApis()).isEqualTo(10);
        assertThat(response.getTotalReviews()).isEqualTo(20);
        assertThat(response.getLatestApis()).containsExactly(mapped);
        assertThat(response.getTopRatedApis()).containsExactly(mapped);
        assertThat(response.getBrazilianApis()).containsExactly(mapped);
    }
}
