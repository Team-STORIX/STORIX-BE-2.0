package com.storix.domain.domains.profile.service;

import com.storix.domain.domains.bannedword.adaptor.BannedWordAdaptor;
import com.storix.domain.domains.genrescore.adaptor.GenreScoreAdaptor;
import com.storix.domain.domains.image.publisher.S3CleanupPublisher;
import com.storix.domain.domains.profile.dto.UserInfo;
import com.storix.domain.domains.profile.dto.UserInfoV2;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.Title;
import com.storix.domain.domains.user.domain.User;
import com.storix.domain.domains.user.exception.me.ProfileForbiddenNicknameException;
import com.storix.domain.domains.user.exception.me.ProfileNicknameBannedWordException;
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
    private final BannedWordAdaptor bannedWordAdaptor;

    // 독자 프로필 조회 (V1)
    @Transactional(readOnly = true)
    public UserInfo getReaderProfileInfo(Long userId) {
        User readerUser = userAdaptor.findUserById(userId);

        return UserInfo.of(readerUser, baseUrl);
    }

    // 독자 프로필 조회 (V2)
    @Transactional(readOnly = true)
    public UserInfoV2 getReaderProfileInfoV2(Long userId) {
        User readerUser = userAdaptor.findUserById(userId);

        Title title = readerUser.getTitle();
        Genre topGenre = title == null ? null : title.getGenre();
        long score = topGenre == null ? 0L : genreScoreAdaptor.findRawScore(userId, topGenre);

        return UserInfoV2.of(readerUser, score, baseUrl);
    }

    // 독자 닉네임 중복 체크
    @Transactional(readOnly = true)
    public void validNickname(String nickName, Long userId) {
        validateNicknameNotBanned(nickName);
        userAdaptor.checkNicknameDuplicateExceptSelf(nickName, userId);
    }

    // 독자 닉네임 변경
    @Transactional
    public String changeNickname(String nickName, Long userId) {
        validateNicknameNotBanned(nickName);
        User readerUser = userAdaptor.findUserById(userId);
        readerUser.changeNickName(nickName);
        return nickName;
    }

    // 닉네임 금칙어/예약어 검증
    private void validateNicknameNotBanned(String nickName) {
        if (bannedWordAdaptor.containsAdminKeyword(nickName)) {
            throw ProfileForbiddenNicknameException.EXCEPTION;
        }
        if (bannedWordAdaptor.containsBannedWord(nickName)) {
            throw ProfileNicknameBannedWordException.EXCEPTION;
        }
    }

    // 독자 한 줄 소개 변경
    @Transactional
    public String changeDescription(String profileDescription, Long userId) {
        User readerUser = userAdaptor.findUserById(userId);
        readerUser.changeProfileDescription(profileDescription);
        return profileDescription;
    }

    // 서재 공개 여부 변경
    @Transactional
    public boolean changeLibraryVisibility(boolean isPublic, Long userId) {
        User readerUser = userAdaptor.findUserById(userId);
        readerUser.changeLibraryVisibility(isPublic);
        return isPublic;
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
