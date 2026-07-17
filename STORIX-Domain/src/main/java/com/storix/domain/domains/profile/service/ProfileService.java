package com.storix.domain.domains.profile.service;

import com.storix.domain.domains.genrescore.adaptor.GenreScoreAdaptor;
import com.storix.domain.domains.image.publisher.S3CleanupPublisher;
import com.storix.domain.domains.profile.dto.UserInfo;
import com.storix.domain.domains.profile.dto.UserInfoV2;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.Title;
import com.storix.domain.domains.user.domain.TitleStage;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.works.domain.Genre;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    @Value("${AWS_S3_BASE_URL}") private String baseUrl;

    private final UserAdaptor userAdaptor;
    private final GenreScoreAdaptor genreScoreAdaptor;
    private final S3CleanupPublisher s3CleanupPublisher;

    // 독자 프로필 조회 (V1)
    @Transactional(readOnly = true)
    public UserInfo getReaderProfileInfo(Long userId) {
        User readerUser = userAdaptor.findUserById(userId);

        return UserInfo.builder()
                .userId(userId)
                .role(readerUser.getRole().toString())
                .nickName(readerUser.getNickName())
                .level(1)// level 미사용
                .point(readerUser.getPoint())
                .profileDescription(readerUser.getProfileDescription())
                .profileImageUrl(readerUser.getProfileObjectKey() == null
                        ? null : baseUrl + "/" + readerUser.getProfileObjectKey())
                .oauthProvider(readerUser.getOauthInfo() == null
                        ? null : readerUser.getOauthInfo().getProvider().getDbValue())
                .build();
    }

    // 독자 프로필 조회 (V2)
    @Transactional(readOnly = true)
    public UserInfoV2 getReaderProfileInfoV2(Long userId) {
        User readerUser = userAdaptor.findUserById(userId);

        Title title = readerUser.getTitle();
        Genre topGenre = title == null ? null : title.getGenre();
        long score = topGenre == null ? 0L : genreScoreAdaptor.findRawScore(userId, topGenre);

        TitleStage stage = title == null ? TitleStage.NONE : title.getStage();
        Integer remainingScore = title == null || stage.isMax() ? null : stage.getNextScore() - (int) score;
        String nextStage = title == null ? null : stage.next().map(TitleStage::getLabel).orElse(null);
        double progressPercentage = title == null ? 0.0 : stage.progressPercentage(score);

        return UserInfoV2.builder()
                .userId(userId)
                .role(readerUser.getRole().toString())
                .nickName(readerUser.getNickName())
                .point(readerUser.getPoint())
                .profileDescription(readerUser.getProfileDescription())
                .profileImageUrl(readerUser.getProfileObjectKey() == null
                        ? null : baseUrl + "/" + readerUser.getProfileObjectKey())
                .oauthProvider(readerUser.getOauthInfo() == null
                        ? null : readerUser.getOauthInfo().getProvider().getDbValue())
                .topGenre(topGenre == null ? null : topGenre.getDbValue())
                .title(title == null ? null : title.getDisplayName())
                .stage(stage.getLabel())
                .nextStage(nextStage)
                .topGenreScore(score)
                .remainingScore(remainingScore)
                .progressPercentage(progressPercentage)
                .build();
    }

    // 독자 닉네임 중복 체크
    @Transactional(readOnly = true)
    public void validNickname(String nickName, Long userId) {
        userAdaptor.checkNicknameDuplicateExceptSelf(nickName, userId);
    }

    // 독자 닉네임 변경
    @Transactional
    public String changeNickname(String nickName, Long userId) {
        User readerUser = userAdaptor.findUserById(userId);
        readerUser.changeNickName(nickName);
        return nickName;
    }

    // 독자 한 줄 소개 변경
    @Transactional
    public String changeDescription(String profileDescription, Long userId) {
        User readerUser = userAdaptor.findUserById(userId);
        readerUser.changeProfileDescription(profileDescription);
        return profileDescription;
    }

    // 프로필 사진 변경
    @Transactional
    public String changeProfileImage(String objectKey, Long userId) {
        User user = userAdaptor.findUserByIdForUpdate(userId);
        String previousObjectKey = user.getProfileObjectKey();
        user.changeProfileImage(objectKey);

        if (previousObjectKey != null && !previousObjectKey.equals(objectKey)) {
            s3CleanupPublisher.publish(previousObjectKey);
        }
        return baseUrl + "/" + objectKey;
    }
}
