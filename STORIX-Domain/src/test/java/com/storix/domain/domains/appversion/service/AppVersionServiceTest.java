package com.storix.domain.domains.appversion.service;

import com.storix.common.property.AppVersionProperties;
import com.storix.domain.domains.appversion.domain.VersionStatus;
import com.storix.domain.domains.pushdevice.domain.OSPlatform;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[앱 버전] 게이팅 판정")
class AppVersionServiceTest {

    private static final String MIN_SUPPORTED = "1.0.5";
    private static final String LATEST = "1.0.7";

    private final AppVersionService appVersionService = new AppVersionService(
            new AppVersionProperties(
                    new AppVersionProperties.Platform(MIN_SUPPORTED, LATEST),
                    new AppVersionProperties.Platform(MIN_SUPPORTED, LATEST)));

    private VersionStatus check(OSPlatform platform, String version) {
        return appVersionService.check(platform, version).status();
    }

    @Nested
    @DisplayName("차단 버전")
    class Blocked {

        @Test
        @DisplayName("안드로이드 1.1.0 은 최신보다 높아도 강제 업데이트다")
        void android_bad_build_is_required() {
            assertThat(check(OSPlatform.ANDROID, "1.1.0"))
                    .isEqualTo(VersionStatus.UPDATE_REQUIRED);
        }

        @Test
        @DisplayName("같은 번호라도 iOS 는 막지 않는다")
        void ios_same_number_is_untouched() {
            assertThat(check(OSPlatform.IOS, "1.1.0"))
                    .isEqualTo(VersionStatus.LATEST);
        }

        @Test
        @DisplayName("차단 대상이 아닌 상위 버전은 정상이다")
        void other_newer_version_is_latest() {
            assertThat(check(OSPlatform.ANDROID, "1.1.1"))
                    .isEqualTo(VersionStatus.LATEST);
        }
    }

    @Nested
    @DisplayName("기존 판정")
    class Existing {

        @Test
        @DisplayName("최소 지원 미만이면 강제 업데이트")
        void below_min_supported() {
            assertThat(check(OSPlatform.ANDROID, "1.0.4"))
                    .isEqualTo(VersionStatus.UPDATE_REQUIRED);
        }

        @Test
        @DisplayName("최신 미만이면 업데이트 권장")
        void below_latest() {
            assertThat(check(OSPlatform.ANDROID, "1.0.6"))
                    .isEqualTo(VersionStatus.UPDATE_AVAILABLE);
        }

        @Test
        @DisplayName("최신이면 LATEST")
        void latest() {
            assertThat(check(OSPlatform.ANDROID, LATEST))
                    .isEqualTo(VersionStatus.LATEST);
        }
    }
}
