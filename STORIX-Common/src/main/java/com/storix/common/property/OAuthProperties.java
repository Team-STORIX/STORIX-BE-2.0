package com.storix.common.property;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

    private final KakaoProperties kakao;
    private final NaverProperties naver;
    private final AppleProperties apple;

    public OAuthProperties(KakaoProperties kakao, NaverProperties naver, AppleProperties apple) {
        this.kakao = kakao;
        this.naver = naver;
        this.apple = apple;
    }

}
