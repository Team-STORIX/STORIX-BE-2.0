package com.storix.storix_api.domains.user.application.service;

import com.storix.storix_api.domains.user.adaptor.AuthUserDetails;
import com.storix.storix_api.domains.user.application.usecase.helper.OAuthHelper;
import com.storix.storix_api.domains.user.adaptor.UserAdaptor;
import com.storix.storix_api.domains.user.domain.OAuthInfo;
import com.storix.storix_api.domains.user.domain.OAuthProvider;
import com.storix.storix_api.domains.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReaderLoginService {

    private final UserAdaptor userAdaptor;
    private final OAuthHelper oauthHelper;

    // 회원가입한 경우 로그인 처리
    public AuthUserDetails execute(String idToken, OAuthProvider provider) {
        OAuthInfo oauthInfo = oauthHelper.getOauthInfoByIdToken(idToken, provider);

        User user = userAdaptor.findReaderUserByOAuthInfo(oauthInfo);
        user.login();
        return new AuthUserDetails(user.getId(), user.getRole());
    }

    // 회원가입하지 않은 경우
    public OAuthInfo getOauthInfoByIdToken(String idToken, OAuthProvider provider) {
        return oauthHelper.getOauthInfoByIdToken(idToken, provider);
    }
}