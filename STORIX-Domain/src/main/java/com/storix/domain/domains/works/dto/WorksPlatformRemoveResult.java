package com.storix.domain.domains.works.dto;

import com.storix.domain.domains.works.domain.Platform;
import com.storix.domain.domains.works.domain.WorksPlatform;

import java.util.Collection;
import java.util.List;

public record WorksPlatformRemoveResult(
        Long worksId,
        boolean dryRun,
        List<Removed> removed,
        List<Platform> notFound,
        List<Platform> remaining
) {

    public record Removed(Platform platform, String landingUrl) {
    }

    // before: 떼기 전 플랫폼, removed: 뗀(dryRun 이면 뗄) 플랫폼
    public static WorksPlatformRemoveResult of(Long worksId, boolean dryRun, Collection<Platform> targets,
                                               Collection<WorksPlatform> before, List<WorksPlatform> removed) {
        List<Platform> removedPlatforms = removed.stream().map(WorksPlatform::getPlatform).toList();
        return new WorksPlatformRemoveResult(
                worksId,
                dryRun,
                removed.stream().map(platform -> new Removed(platform.getPlatform(), platform.getLandingUrl())).toList(),
                targets.stream().filter(platform -> !removedPlatforms.contains(platform)).toList(),
                before.stream().map(WorksPlatform::getPlatform).filter(platform -> !removedPlatforms.contains(platform)).sorted().toList()
        );
    }
}
