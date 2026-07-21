package com.storix.domain.domains.appversion.service;

import com.storix.common.property.AppVersionProperties;
import com.storix.domain.domains.appversion.domain.VersionStatus;
import com.storix.domain.domains.appversion.dto.AppVersionCheck;
import com.storix.domain.domains.appversion.exception.InvalidAppVersionException;
import com.storix.domain.domains.pushdevice.domain.OSPlatform;
import lombok.RequiredArgsConstructor;
import org.semver4j.Semver;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppVersionService {

    private final AppVersionProperties appVersionProperties;

    public AppVersionCheck check(OSPlatform platform, String clientVersion) {
        AppVersionProperties.Platform cfg = platform == OSPlatform.IOS
                ? appVersionProperties.getIos()
                : appVersionProperties.getAndroid();

        VersionStatus status = resolveStatus(parse(clientVersion), cfg);
        return new AppVersionCheck(status, cfg.getLatest(), cfg.getMinSupported());
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
