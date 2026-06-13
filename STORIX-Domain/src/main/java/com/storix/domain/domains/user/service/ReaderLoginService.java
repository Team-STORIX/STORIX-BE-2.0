package com.storix.domain.domains.user.service;

import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.OAuthInfo;
import com.storix.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReaderLoginService {

    private final UserAdaptor userAdaptor;

    // 회원가입한 경우 로그인 처리
    @Transactional
    public AuthUserDetails execute(OAuthInfo oauthInfo, String oauthRefreshToken) {
        User user = userAdaptor.findReaderUserByOAuthInfo(oauthInfo);
        user.login();
        user.updateOauthRefreshToken(oauthRefreshToken);
        return new AuthUserDetails(user.getId(), user.getRole());
    }

}
