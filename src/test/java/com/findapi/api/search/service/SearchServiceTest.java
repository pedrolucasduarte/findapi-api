package com.findapi.api.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import com.findapi.api.apiCatalog.repository.ApiRepository;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.search.dto.request.ApiSearchRequest;
import com.findapi.api.search.dto.response.ApiSearchResponse;
import com.findapi.api.search.mapper.SearchMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {
    @Mock private ApiRepository apiRepository;
    @Mock private SearchMapper searchMapper;

    @Test
    @SuppressWarnings("unchecked")
    void searchWithRelationshipFiltersAndLimitPageSize() {
        ApiEntity entity = new ApiEntity();
        ApiSearchResponse mapped = ApiSearchResponse.builder().id(UUID.randomUUID()).build();
        when(apiRepository.findAll(
                ArgumentMatchers.<Specification<ApiEntity>>any(),
                any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 100), 1));
        when(searchMapper.toResponse(entity)).thenReturn(mapped);
        SearchService service = new SearchService(apiRepository, searchMapper);
        ApiSearchRequest request = ApiSearchRequest.builder()
                .name("weather")
                .categoryId(UUID.randomUUID())
                .tagId(UUID.randomUUID())
                .build();

        var response = service.search(request, PageRequest.of(0, 500));

        assertThat(response.content()).containsExactly(mapped);
        assertThat(response.size()).isEqualTo(100);
    }
}
