package com.storix.domain.domains.appversion.service;

import com.storix.common.property.AppVersionProperties;
import com.storix.domain.domains.appversion.domain.VersionStatus;
import com.storix.domain.domains.pushdevice.domain.OSPlatform;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[앱 버전] 게이팅 판정")
class AppVersionServiceTest {

    private static final String MIN_SUPPORTED = "1.0.5";
    private static final String LATEST = "1.0.7";
    private static final LocalDate RELEASE_DATE = LocalDate.of(2026, 8, 16);

    private AppVersionService serviceOn(String profile) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.setActiveProfiles(profile);
        return new AppVersionService(
                new AppVersionProperties(
                        new AppVersionProperties.Platform(MIN_SUPPORTED, LATEST, RELEASE_DATE),
                        new AppVersionProperties.Platform(MIN_SUPPORTED, LATEST, RELEASE_DATE)),
                environment);
    }

    private VersionStatus check(AppVersionService service, OSPlatform platform, String version) {
        return service.check(platform, version).status();
    }

    @Nested
    @DisplayName("차단 버전")
    class Blocked {

        private final AppVersionService devService = serviceOn("dev");

        @Test
        @DisplayName("dev 에서 안드로이드 1.1.0 은 최신보다 높아도 강제 업데이트다")
        void android_bad_build_is_required_on_dev() {
            assertThat(check(devService, OSPlatform.ANDROID, "1.1.0"))
                    .isEqualTo(VersionStatus.UPDATE_REQUIRED);
        }

        @Test
        @DisplayName("같은 번호라도 iOS 는 막지 않는다")
        void ios_same_number_is_untouched() {
            assertThat(check(devService, OSPlatform.IOS, "1.1.0"))
                    .isEqualTo(VersionStatus.LATEST);
        }

        @Test
        @DisplayName("차단 대상이 아닌 상위 버전은 정상이다")
        void other_newer_version_is_latest() {
            assertThat(check(devService, OSPlatform.ANDROID, "1.1.1"))
                    .isEqualTo(VersionStatus.LATEST);
        }

        @Test
        @DisplayName("isBlocked 도 dev 에서만 true 다")
        void is_blocked_only_on_dev() {
            assertThat(devService.isBlocked(OSPlatform.ANDROID, "1.1.0")).isTrue();
            assertThat(serviceOn("prod").isBlocked(OSPlatform.ANDROID, "1.1.0")).isFalse();
        }
    }

    @Nested
    @DisplayName("dev 가 아닌 환경")
    class OtherProfiles {

        @Test
        @DisplayName("prod 에서는 차단하지 않는다")
        void prod_is_untouched() {
            assertThat(check(serviceOn("prod"), OSPlatform.ANDROID, "1.1.0"))
                    .isEqualTo(VersionStatus.LATEST);
        }

        @Test
        @DisplayName("local 에서도 차단하지 않는다")
        void local_is_untouched() {
            assertThat(check(serviceOn("local"), OSPlatform.ANDROID, "1.1.0"))
                    .isEqualTo(VersionStatus.LATEST);
        }
    }

    @Nested
    @DisplayName("기존 판정")
    class Existing {

        private final AppVersionService service = serviceOn("dev");

        @Test
        @DisplayName("최소 지원 미만이면 강제 업데이트")
        void below_min_supported() {
            assertThat(check(service, OSPlatform.ANDROID, "1.0.4"))
                    .isEqualTo(VersionStatus.UPDATE_REQUIRED);
        }

        @Test
        @DisplayName("최신 미만이면 업데이트 권장")
        void below_latest() {
            assertThat(check(service, OSPlatform.ANDROID, "1.0.6"))
                    .isEqualTo(VersionStatus.UPDATE_AVAILABLE);
        }

        @Test
        @DisplayName("최신이면 LATEST")
        void latest() {
            assertThat(check(service, OSPlatform.ANDROID, LATEST))
                    .isEqualTo(VersionStatus.LATEST);
        }

        @Test
        @DisplayName("설정된 배포일을 그대로 내려준다")
        void exposes_release_date() {
            assertThat(service.check(OSPlatform.ANDROID, "1.0.6").releaseDate())
                    .isEqualTo(RELEASE_DATE);
        }
    }
}
