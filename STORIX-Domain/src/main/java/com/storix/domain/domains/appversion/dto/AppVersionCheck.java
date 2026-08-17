package com.storix.domain.domains.appversion.dto;

import com.storix.domain.domains.appversion.domain.VersionStatus;

import java.time.LocalDate;

public record AppVersionCheck(
        VersionStatus status,
        String latestVersion,
        String minSupportedVersion,
        LocalDate releaseDate
) {
}
