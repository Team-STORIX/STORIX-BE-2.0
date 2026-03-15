package com.storix.domain.domains.plus.service;

import com.storix.domain.domains.library.adaptor.LibraryAdaptor;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.plus.domain.Review;
import com.storix.domain.domains.plus.dto.CreateReviewCommand;
import com.storix.domain.domains.plus.dto.ReaderReviewRedirectResponse;
import com.storix.domain.domains.plus.dto.ReviewInfo;
import com.storix.domain.domains.plus.dto.SliceReviewInfo;
import com.storix.domain.domains.review.adaptor.ReviewLikeAdaptor;
import com.storix.domain.domains.review.dto.DetailedReviewInfoWithProfile;
import com.storix.domain.domains.review.dto.SliceReviewInfoWithProfile;
import com.storix.domain.domains.review.dto.StandardReviewInfo;
import com.storix.domain.domains.review.dto.StandardSliceReviewInfo;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.dto.StandardProfileInfo;
import com.storix.domain.domains.works.application.helper.AdultWorksHelper;
import com.storix.domain.domains.works.application.port.LoadWorksPort;
import com.storix.domain.domains.works.dto.StandardWorksInfo;
import com.storix.domain.domains.works.dto.WorksInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final UserAdaptor userAdaptor;
    private final ReviewAdaptor reviewAdaptor;
    private final ReviewLikeAdaptor reviewLikeAdaptor;
    private final LibraryAdaptor libraryAdaptor;

    private final LoadWorksPort loadWorksPort;

    private final AdultWorksHelper adultWorksHelper;

    // 플러스탭
    @Transactional
    public ReaderReviewRedirectResponse createReview(CreateReviewCommand cmd) {

        reviewAdaptor.existsByUserAndWorks(cmd.libraryUserId(), cmd.worksId());

        // 성인 작품 여부 확인 및 핸들링
        adultWorksHelper.CheckUserAuthorityWithWorks(cmd.libraryUserId(), cmd.worksId());

        Review review = reviewAdaptor.saveReview(cmd);

        // 서재 도메인 업데이트
        libraryAdaptor.incrementReviewCount(cmd.libraryUserId());

        // 작품 도메인 업데이트
        loadWorksPort.updateIncrementingReviewInfoToWorks(cmd.worksId(), cmd.rating().getRatingValue());

        return new ReaderReviewRedirectResponse(review.getWorksId(), review.getLibraryUserId(), review.getId());
    }

    @Transactional
    public void isReviewExist(Long userId, Long worksId) {

        // 리뷰 중복 확인
        reviewAdaptor.existsByUserAndWorks(userId, worksId);
    }

    // 작품 상세탭
    @Transactional
    public SliceReviewInfo findMyReview(Long userId, Long worksId) {

        boolean isMyReviewExist = reviewAdaptor.isMyReviewExist(userId, worksId);

        if (isMyReviewExist) {
            return reviewAdaptor.getMyReviewInfo(userId, worksId);
        } else {
            return null;
        }

    }

    @Transactional(readOnly = true)
    public Slice<SliceReviewInfoWithProfile> findAllReviewWithoutMine(Long userId, Long worksId, Pageable pageable) {

        // 1) 다른 사람의 리뷰 정보
        Slice<SliceReviewInfo> reviews = reviewAdaptor.getOtherReviewInfo(userId, worksId, pageable);

        if (reviews.isEmpty()) {
            return new SliceImpl<>(List.of(), pageable, reviews.hasNext());
        }

        // 유저 id 리스트
        List<Long> userIds = reviews.getContent().stream()
                .map(SliceReviewInfo::userId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 2) 프로필 정보
        Map<Long, StandardProfileInfo> profileMap =
                userAdaptor.findStandardProfileInfoByUserIds(userIds);

        // 프로필 조회에 실패할 경우 필터링
        List<SliceReviewInfoWithProfile> content = reviews.getContent().stream()
                .map(review -> {
                    StandardProfileInfo profile = profileMap.get(review.userId());
                    if (profile == null) return null;
                    return SliceReviewInfoWithProfile.of(
                            StandardSliceReviewInfo.from(review),
                            profile
                    );
                })
                .filter(Objects::nonNull)
                .toList();

        return new SliceImpl<>(content, reviews.getPageable(), reviews.hasNext());
    }

    @Transactional(readOnly = true)
    public DetailedReviewInfoWithProfile findReviewDetail(Long userId, Long reviewId) {

        // 1) 리뷰 정보
        ReviewInfo reviewInfo = reviewAdaptor.findReviewById(reviewId);
        boolean isLiked = reviewLikeAdaptor.isAlreadyLiked(userId, reviewId);
        StandardReviewInfo review = StandardReviewInfo.fromReviewInfo(reviewInfo, isLiked);

        // 2) 프로필 정보
        StandardProfileInfo profile =
                userAdaptor.findStandardProfileInfoByUserId(reviewInfo.reviewerId());

        // 3) 작품 정보
        Long worksId = reviewInfo.worksId();

        WorksInfo worksInfo = loadWorksPort.findWorksInfoById(worksId);
        StandardWorksInfo works = StandardWorksInfo.from(worksInfo);

        return DetailedReviewInfoWithProfile.of(profile, works, review);
    }

}