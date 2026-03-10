package com.storix.common.property;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {

    private final KakaoProperties kakao;
    private final NaverProperties naver;

    public OAuthProperties(KakaoProperties kakao, NaverProperties naver) {
        this.kakao = kakao;
        this.naver = naver;
    }

}
