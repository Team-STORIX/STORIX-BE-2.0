package com.storix.domain.domains.profile.service;

import com.storix.domain.domains.profile.dto.UserInfo;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    @Value("${AWS_S3_BASE_URL}") private String baseUrl;

    private final UserAdaptor userAdaptor;

    // 독자 프로필 조회
    @Transactional(readOnly = true)
    public UserInfo getReaderProfileInfo(Long userId) {
        User readerUser = userAdaptor.findUserById(userId);

        return UserInfo.builder()
                .userId(userId)
                .role(readerUser.getRole().toString())
                .nickName(readerUser.getNickName())
                .level(readerUser.getLevel())
                .point(readerUser.getPoint())
                .profileDescription(readerUser.getProfileDescription())
                .profileImageUrl(readerUser.getProfileObjectKey() == null
                        ? null : baseUrl + "/" + readerUser.getProfileObjectKey())
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
