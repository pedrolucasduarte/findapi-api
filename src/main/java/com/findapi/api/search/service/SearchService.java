package com.findapi.api.search.service;

import com.findapi.api.apiCatalog.dto.request.ApiFilterRequest;
import com.findapi.api.apiCatalog.repository.ApiRepository;
import com.findapi.api.apiCatalog.specification.ApiSpecification;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.search.dto.request.ApiSearchRequest;
import com.findapi.api.search.dto.response.ApiSearchResponse;
import com.findapi.api.search.mapper.SearchMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ApiRepository apiRepository;
    private final SearchMapper searchMapper;

    @Transactional(readOnly = true)
    public PageResponse<ApiSearchResponse> search(ApiSearchRequest request, Pageable pageable) {
        ApiSearchRequest safeRequest = request == null ? new ApiSearchRequest() : request;
        Specification<ApiEntity> specification = ApiSpecification.fromFilter(toApiFilter(safeRequest));
        specification = andIfPresent(specification, ApiSpecification.categoryEquals(safeRequest.getCategoryId()));
        specification = andIfPresent(specification, ApiSpecification.tagEquals(safeRequest.getTagId()));
        return PageResponse.from(apiRepository.findAll(specification, safePageable(pageable))
                .map(searchMapper::toResponse));
    }

    private ApiFilterRequest toApiFilter(ApiSearchRequest request) {
        return ApiFilterRequest.builder()
                .name(request.getName())
                .apiType(request.getApiType())
                .authenticationMethodId(request.getAuthenticationMethodId())
                .freeTier(request.getFreeTier())
                .officialSdk(request.getOfficialSdk())
                .openSource(request.getOpenSource())
                .selfHosted(request.getSelfHosted())
                .brazilian(request.getBrazilian())
                .integrationDifficulty(request.getIntegrationDifficulty())
                .status(request.getStatus())
                .build();
    }

    private Specification<ApiEntity> andIfPresent(
            Specification<ApiEntity> current,
            Specification<ApiEntity> candidate
    ) {
        return candidate == null ? current : current.and(candidate);
    }

    private Pageable safePageable(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        int page = Math.max(pageable.getPageNumber(), 0);
        int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "createdAt");
        return PageRequest.of(page, size, sort);
    }
}
