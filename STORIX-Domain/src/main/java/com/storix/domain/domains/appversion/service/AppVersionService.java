package com.storix.domain.domains.appversion.service;

import com.storix.common.property.AppVersionProperties;
import com.storix.domain.domains.appversion.domain.VersionStatus;
import com.storix.domain.domains.appversion.dto.AppVersionCheck;
import com.storix.domain.domains.appversion.exception.InvalidAppVersionException;
import com.storix.domain.domains.pushdevice.domain.OSPlatform;
import lombok.RequiredArgsConstructor;
import org.semver4j.Semver;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppVersionService {

    // 잘못 배포된 빌드. 정식 최신보다 높아 버전 비교로는 안 걸리므로 따로 막는다.
    // 정식 버전이 이 번호에 도달하기 전에 지워야 정상 사용자가 막히지 않는다
    private static final Map<OSPlatform, Set<String>> BLOCKED_VERSIONS = Map.of(
            OSPlatform.ANDROID, Set.of("1.1.0"));

    private final AppVersionProperties appVersionProperties;

    public AppVersionCheck check(OSPlatform platform, String clientVersion) {
        AppVersionProperties.Platform cfg = platform == OSPlatform.IOS
                ? appVersionProperties.getIos()
                : appVersionProperties.getAndroid();

        Semver client = parse(clientVersion);
        VersionStatus status = matchesBlocked(platform, client)
                ? VersionStatus.UPDATE_REQUIRED
                : resolveStatus(client, cfg);
        return new AppVersionCheck(status, cfg.getLatest(), cfg.getMinSupported());
    }

    public boolean isBlocked(OSPlatform platform, String clientVersion) {
        return matchesBlocked(platform, parse(clientVersion));
    }

    private boolean matchesBlocked(OSPlatform platform, Semver client) {
        return BLOCKED_VERSIONS.getOrDefault(platform, Set.of()).stream()
                .anyMatch(blocked -> client.isEqualTo(parse(blocked)));
    }

    private VersionStatus resolveStatus(Semver client, AppVersionProperties.Platform cfg) {
        if (client.isLowerThan(parse(cfg.getMinSupported()))) return VersionStatus.UPDATE_REQUIRED;
        if (client.isLowerThan(parse(cfg.getLatest()))) return VersionStatus.UPDATE_AVAILABLE;
        return VersionStatus.LATEST;
    }

    private Semver parse(String version) {
        Semver semver = Semver.coerce(version);
        if (semver == null) {
            throw InvalidAppVersionException.EXCEPTION;
        }
        return semver;
    }
}
