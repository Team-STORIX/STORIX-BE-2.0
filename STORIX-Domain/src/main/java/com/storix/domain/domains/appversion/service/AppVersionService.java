package com.storix.domain.domains.appversion.service;

import com.storix.common.property.AppVersionProperties;
import com.storix.domain.domains.appversion.domain.VersionStatus;
import com.storix.domain.domains.appversion.dto.AppVersionCheck;
import com.storix.domain.domains.appversion.exception.InvalidAppVersionException;
import com.storix.domain.domains.pushdevice.domain.OSPlatform;
import lombok.RequiredArgsConstructor;
import org.semver4j.Semver;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AppVersionService {

    private static final Map<OSPlatform, Set<String>> BLOCKED_VERSIONS = Map.of(
            OSPlatform.ANDROID, Set.of("1.1.0"));

    private static final String BLOCKED_PROFILE = "dev";

    private final AppVersionProperties appVersionProperties;
    private final Environment environment;

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
        if (!environment.acceptsProfiles(Profiles.of(BLOCKED_PROFILE))) {
            return false;
        }
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
