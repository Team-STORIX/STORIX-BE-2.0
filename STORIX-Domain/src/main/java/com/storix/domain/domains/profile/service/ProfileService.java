package com.storix.domain.domains.profile.service;

import com.storix.domain.domains.genrescore.dto.TopGenreInfo;
import com.storix.domain.domains.genrescore.service.TopGenreResolver;
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
    private final TopGenreResolver topGenreResolver;

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

        // 대표 장르가 없는 유저(활동 0)면 null/0 으로 처리
        TopGenreInfo top = topGenreResolver.resolve(userId).orElse(null);
        Genre topGenre = top == null ? null : top.genre();
        long score = top == null ? 0L : top.score();

        TitleStage stage = TitleStage.from(score);
        Title title = topGenre == null ? null : Title.resolve(topGenre, score).orElse(null);
        Integer remainingScore = stage.isMax() ? null : stage.getNextScore() - (int) score;
        String nextStage = stage.isMax() ? null : stage.next().getLabel();

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
                .progressPercentage(stage.progressPercentage(score))
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
        User user = userAdaptor.findUserById(userId);
        user.changeProfileImage(objectKey);
        return baseUrl + "/" + objectKey;
    }
}
