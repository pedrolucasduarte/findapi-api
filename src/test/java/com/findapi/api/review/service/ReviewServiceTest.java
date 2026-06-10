package com.findapi.api.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.findapi.api.apiCatalog.repository.ApiRepository;
import com.findapi.api.common.exception.BusinessException;
import com.findapi.api.common.exception.ResourceNotFoundException;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.entity.AppUserEntity;
import com.findapi.api.entity.ReviewEntity;
import com.findapi.api.enums.UserRole;
import com.findapi.api.review.dto.request.ReviewCreateRequest;
import com.findapi.api.review.dto.request.ReviewFilterRequest;
import com.findapi.api.review.dto.request.ReviewUpdateRequest;
import com.findapi.api.review.dto.response.ReviewDetailResponse;
import com.findapi.api.review.dto.response.ReviewResponse;
import com.findapi.api.review.mapper.ReviewMapper;
import com.findapi.api.review.repository.ReviewRepository;
import com.findapi.api.user.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    private static final UUID API_ID = UUID.fromString("4033709b-b0f8-4b74-8fd0-8c4d8f9f55f0");
    private static final UUID REVIEW_ID = UUID.fromString("7f2ed709-0326-452a-9d01-833bfbd4ed65");
    private static final UUID USER_ID = UUID.fromString("a9ef0f06-d716-4419-b7a9-58c820cc4894");
    private static final UUID OTHER_USER_ID = UUID.fromString("bb275dbe-65e0-475c-b714-f9b8d5ff9db6");

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ApiRepository apiRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewMapper reviewMapper;

    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository, apiRepository, userRepository, reviewMapper);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createReviewSuccessfully() {
        authenticate(USER_ID, "ROLE_USER");
        ReviewCreateRequest request = createRequest();
        ReviewDetailResponse expected = detailResponse(USER_ID, 5);

        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.of(apiEntity()));
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(userEntity(USER_ID)));
        when(reviewRepository.existsByApiIdAndUserIdAndDeletedAtIsNull(API_ID, USER_ID)).thenReturn(false);
        when(reviewRepository.save(any(ReviewEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reviewMapper.entityToDetailResponse(any(ReviewEntity.class))).thenReturn(expected);

        ReviewDetailResponse response = reviewService.create(API_ID, request);

        ArgumentCaptor<ReviewEntity> captor = ArgumentCaptor.forClass(ReviewEntity.class);
        verify(reviewRepository).save(captor.capture());
        assertThat(response).isEqualTo(expected);
        assertThat(captor.getValue().getApi().getId()).isEqualTo(API_ID);
        assertThat(captor.getValue().getUser().getId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getRating()).isEqualTo((short) 5);
        assertThat(captor.getValue().getComment()).isEqualTo("Great API");
    }

    @Test
    void blockDuplicatedReview() {
        authenticate(USER_ID, "ROLE_USER");
        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.of(apiEntity()));
        when(userRepository.findByIdAndDeletedAtIsNull(USER_ID)).thenReturn(Optional.of(userEntity(USER_ID)));
        when(reviewRepository.existsByApiIdAndUserIdAndDeletedAtIsNull(API_ID, USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> reviewService.create(API_ID, createRequest()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("User has already reviewed this API.");
    }

    @Test
    void blockCreateWhenApiDoesNotExist() {
        authenticate(USER_ID, "ROLE_USER");
        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.create(API_ID, createRequest()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("API not found.");
    }

    @Test
    void findReviewByIdSuccessfully() {
        ReviewEntity entity = activeEntity(USER_ID, 5);
        ReviewDetailResponse expected = detailResponse(USER_ID, 5);

        when(reviewRepository.findByIdAndDeletedAtIsNull(REVIEW_ID)).thenReturn(Optional.of(entity));
        when(reviewMapper.entityToDetailResponse(entity)).thenReturn(expected);

        assertThat(reviewService.findById(REVIEW_ID)).isEqualTo(expected);
    }

    @Test
    void listReviewsByApiId() {
        ReviewEntity entity = activeEntity(USER_ID, 5);
        ReviewResponse mapped = response(USER_ID, 5);

        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.of(apiEntity()));
        when(reviewRepository.findByApiIdAndDeletedAtIsNull(API_ID)).thenReturn(List.of(entity));
        when(reviewMapper.entityToResponse(entity)).thenReturn(mapped);

        assertThat(reviewService.findByApiId(API_ID)).containsExactly(mapped);
    }

    @Test
    void listReviewsWithFilters() {
        ReviewEntity entity = activeEntity(USER_ID, 5);
        ReviewResponse mapped = response(USER_ID, 5);
        ReviewFilterRequest filter = ReviewFilterRequest.builder()
                .apiId(API_ID)
                .userId(USER_ID)
                .rating(5)
                .build();
        PageRequest request = PageRequest.of(0, 200, Sort.unsorted());

        when(reviewRepository.findAll(
                ArgumentMatchers.<Specification<ReviewEntity>>any(),
                any(PageRequest.class)))
                .thenReturn(new PageImpl<>(
                        List.of(entity),
                        PageRequest.of(0, 100, Sort.by("createdAt").descending()),
                        1
                ));
        when(reviewMapper.entityToResponse(entity)).thenReturn(mapped);

        PageResponse<ReviewResponse> page = reviewService.findAll(filter, request);

        assertThat(page.content()).containsExactly(mapped);
        assertThat(page.size()).isEqualTo(100);
    }

    @Test
    void updateReviewAsAuthor() {
        authenticate(USER_ID, "ROLE_USER");
        ReviewEntity entity = activeEntity(USER_ID, 3);
        ReviewUpdateRequest request = updateRequest();
        ReviewDetailResponse expected = detailResponse(USER_ID, 4);

        when(reviewRepository.findByIdAndDeletedAtIsNull(REVIEW_ID)).thenReturn(Optional.of(entity));
        when(reviewRepository.save(entity)).thenReturn(entity);
        when(reviewMapper.entityToDetailResponse(entity)).thenReturn(expected);

        ReviewDetailResponse response = reviewService.update(REVIEW_ID, request);

        assertThat(response).isEqualTo(expected);
        assertThat(entity.getRating()).isEqualTo((short) 4);
        assertThat(entity.getComment()).isEqualTo("Updated review");
    }

    @Test
    void blockUpdateByAnotherUser() {
        authenticate(OTHER_USER_ID, "ROLE_USER");
        when(reviewRepository.findByIdAndDeletedAtIsNull(REVIEW_ID)).thenReturn(Optional.of(activeEntity(USER_ID, 5)));

        assertThatThrownBy(() -> reviewService.update(REVIEW_ID, updateRequest()))
                .isInstanceOf(AuthorizationDeniedException.class)
                .hasMessage("Access denied.");
    }

    @Test
    void deleteReviewAsAuthor() {
        authenticate(USER_ID, "ROLE_USER");
        ReviewEntity entity = activeEntity(USER_ID, 5);
        when(reviewRepository.findByIdAndDeletedAtIsNull(REVIEW_ID)).thenReturn(Optional.of(entity));

        reviewService.delete(REVIEW_ID);

        assertThat(entity.getDeletedAt()).isNotNull();
        verify(reviewRepository).save(entity);
    }

    @Test
    void blockDeleteByAnotherUser() {
        authenticate(OTHER_USER_ID, "ROLE_USER");
        when(reviewRepository.findByIdAndDeletedAtIsNull(REVIEW_ID)).thenReturn(Optional.of(activeEntity(USER_ID, 5)));

        assertThatThrownBy(() -> reviewService.delete(REVIEW_ID))
                .isInstanceOf(AuthorizationDeniedException.class)
                .hasMessage("Access denied.");
    }

    @Test
    void calculateAverageRatingCorrectly() {
        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.of(apiEntity()));
        when(reviewRepository.calculateAverageRating(API_ID)).thenReturn(4.333);

        assertThat(reviewService.calculateAverageRating(API_ID)).isEqualByComparingTo(new BigDecimal("4.33"));
    }

    @Test
    void calculateReviewCountCorrectly() {
        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.of(apiEntity()));
        when(reviewRepository.calculateReviewCount(API_ID)).thenReturn(7L);

        assertThat(reviewService.calculateReviewCount(API_ID)).isEqualTo(7L);
    }

    private void authenticate(UUID userId, String authority) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(userId.toString(), "token", authority);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private ReviewCreateRequest createRequest() {
        return ReviewCreateRequest.builder()
                .rating(5)
                .comment(" Great API ")
                .build();
    }

    private ReviewUpdateRequest updateRequest() {
        return ReviewUpdateRequest.builder()
                .rating(4)
                .comment(" Updated review ")
                .build();
    }

    private ReviewEntity activeEntity(UUID userId, int rating) {
        ReviewEntity entity = new ReviewEntity();
        entity.setId(REVIEW_ID);
        entity.setApi(apiEntity());
        entity.setUser(userEntity(userId));
        entity.setRating((short) rating);
        entity.setComment("Great API");
        return entity;
    }

    private ApiEntity apiEntity() {
        ApiEntity api = new ApiEntity();
        api.setId(API_ID);
        api.setName("Find API");
        api.setSlug("find-api");
        return api;
    }

    private AppUserEntity userEntity(UUID userId) {
        AppUserEntity user = new AppUserEntity();
        user.setId(userId);
        user.setName("Reviewer");
        user.setEmail("%s@example.com".formatted(userId));
        user.setPasswordHash("hash");
        user.setRole(UserRole.USER);
        return user;
    }

    private ReviewDetailResponse detailResponse(UUID userId, int rating) {
        return ReviewDetailResponse.builder()
                .id(REVIEW_ID)
                .apiId(API_ID)
                .userId(userId)
                .rating(rating)
                .comment("Great API")
                .build();
    }

    private ReviewResponse response(UUID userId, int rating) {
        return ReviewResponse.builder()
                .id(REVIEW_ID)
                .apiId(API_ID)
                .userId(userId)
                .rating(rating)
                .comment("Great API")
                .build();
    }
}
