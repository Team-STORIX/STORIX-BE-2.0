package com.storix.domain.domains.user.service;

import com.storix.domain.domains.user.adaptor.AuthUserDetails;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.domain.OAuthInfo;
import com.storix.domain.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReaderLoginService {

    private final UserAdaptor userAdaptor;

    // 회원가입한 경우 로그인 처리
    public AuthUserDetails execute(OAuthInfo oauthInfo) {
        User user = userAdaptor.findReaderUserByOAuthInfo(oauthInfo);
        user.login();
        return new AuthUserDetails(user.getId(), user.getRole());
    }

}
