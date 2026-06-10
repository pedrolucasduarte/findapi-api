package com.findapi.api.review.specification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.findapi.api.TestcontainersConfiguration;
import com.findapi.api.apiCatalog.repository.ApiRepository;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.entity.AppUserEntity;
import com.findapi.api.entity.AuthenticationMethodEntity;
import com.findapi.api.entity.ReviewEntity;
import com.findapi.api.enums.ApiStatus;
import com.findapi.api.enums.ApiType;
import com.findapi.api.enums.IntegrationDifficulty;
import com.findapi.api.enums.UserRole;
import com.findapi.api.review.dto.request.ReviewFilterRequest;
import com.findapi.api.review.repository.ReviewRepository;
import com.findapi.api.user.repository.UserRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class ReviewSpecificationTest {
    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void filtersByApiId() {
        AuthenticationMethodEntity authenticationMethod = authenticationMethod();
        ApiEntity weatherApi = saveApi("Weather API", "review-weather-api", authenticationMethod);
        ApiEntity paymentApi = saveApi("Payment API", "review-payment-api", authenticationMethod);
        AppUserEntity user = saveUser("api-id-user@example.com");
        ReviewEntity target = saveReview(weatherApi, user, 5);
        saveReview(paymentApi, user, 4);

        ReviewFilterRequest filter = ReviewFilterRequest.builder()
                .apiId(weatherApi.getId())
                .build();

        List<ReviewEntity> result = reviewRepository.findAll(ReviewSpecification.fromFilter(filter));

        assertThat(result).extracting(ReviewEntity::getId).containsExactly(target.getId());
    }

    @Test
    void filtersByRatingAndUserId() {
        AuthenticationMethodEntity authenticationMethod = authenticationMethod();
        ApiEntity api = saveApi("Rating API", "review-rating-api", authenticationMethod);
        AppUserEntity targetUser = saveUser("target-reviewer@example.com");
        AppUserEntity otherUser = saveUser("other-reviewer@example.com");
        ReviewEntity target = saveReview(api, targetUser, 5);
        saveReview(api, otherUser, 4);

        ReviewFilterRequest filter = ReviewFilterRequest.builder()
                .rating(5)
                .userId(targetUser.getId())
                .build();

        List<ReviewEntity> result = reviewRepository.findAll(ReviewSpecification.fromFilter(filter));

        assertThat(result).extracting(ReviewEntity::getId).containsExactly(target.getId());
    }

    @Test
    void alwaysAppliesDeletedAtIsNull() {
        AuthenticationMethodEntity authenticationMethod = authenticationMethod();
        ApiEntity api = saveApi("Soft Review API", "review-soft-api", authenticationMethod);
        AppUserEntity activeUser = saveUser("active-reviewer@example.com");
        AppUserEntity deletedUser = saveUser("deleted-reviewer@example.com");
        ReviewEntity active = saveReview(api, activeUser, 5);
        ReviewEntity deleted = saveReview(api, deletedUser, 3);
        deleted.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        reviewRepository.saveAndFlush(deleted);

        List<ReviewEntity> result = reviewRepository.findAll(ReviewSpecification.fromFilter(null));

        assertThat(result).extracting(ReviewEntity::getId).contains(active.getId());
        assertThat(result).extracting(ReviewEntity::getId).doesNotContain(deleted.getId());
    }

    private ReviewEntity saveReview(ApiEntity api, AppUserEntity user, int rating) {
        ReviewEntity entity = new ReviewEntity();
        entity.setApi(api);
        entity.setUser(user);
        entity.setRating((short) rating);
        entity.setComment("Useful API");
        return reviewRepository.saveAndFlush(entity);
    }

    private AppUserEntity saveUser(String email) {
        AppUserEntity entity = new AppUserEntity();
        entity.setName("Reviewer");
        entity.setEmail(email);
        entity.setPasswordHash("hash");
        entity.setRole(UserRole.USER);
        return userRepository.saveAndFlush(entity);
    }

    private ApiEntity saveApi(String name, String slug, AuthenticationMethodEntity authenticationMethod) {
        ApiEntity entity = new ApiEntity();
        entity.setAuthenticationMethod(authenticationMethod);
        entity.setName(name);
        entity.setSlug(slug);
        entity.setShortDescription("Public integration API");
        entity.setFullDescription("Detailed description");
        entity.setOfficialSite("https://example.com");
        entity.setDocumentationUrl("https://docs.example.com");
        entity.setApiType(ApiType.PUBLIC);
        entity.setStatus(ApiStatus.ACTIVE);
        entity.setFreeTier(true);
        entity.setOfficialSdk(true);
        entity.setOpenSource(false);
        entity.setSelfHosted(false);
        entity.setBrazilian(false);
        entity.setIntegrationDifficulty(IntegrationDifficulty.EASY);
        return apiRepository.saveAndFlush(entity);
    }

    private AuthenticationMethodEntity authenticationMethod() {
        return entityManager
                .createQuery(
                        "select method from AuthenticationMethodEntity method where method.name = :name",
                        AuthenticationMethodEntity.class
                )
                .setParameter("name", "API_KEY")
                .getSingleResult();
    }
}
