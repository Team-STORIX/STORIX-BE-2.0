package com.storix.api.domain.aichat.stream.dto;

import com.storix.api.domain.aichat.stream.AiChatRawWork;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public record AiServerWorksEvent(
        List<AiServerRecommendedWork> recommendedWorks,
        List<Long> candidateWorkIds,
        String pipelineVersion
) {
    // 후보 안 · 서재 밖 · 중복 없이 max 개까지만 믿는다. 순위는 AI 순서대로 1 부터 다시 매긴다
    public List<AiChatRawWork> trustedWorks(Collection<Long> excludedWorkIds, int max) {
        Set<Long> candidates = candidateWorkIds == null ? Set.of() : new HashSet<>(candidateWorkIds);
        Set<Long> excluded = new HashSet<>(excludedWorkIds);
        Set<Long> seen = new HashSet<>();
        List<AiServerRecommendedWork> trusted = (recommendedWorks == null ? List.<AiServerRecommendedWork>of() : recommendedWorks)
                .stream()
                .sorted(Comparator.comparingInt(AiServerRecommendedWork::rank))
                .filter(work -> work.workId() != null
                        && candidates.contains(work.workId())
                        && !excluded.contains(work.workId())
                        && seen.add(work.workId()))
                .limit(max)
                .toList();
        return IntStream.range(0, trusted.size())
                .mapToObj(i -> new AiChatRawWork(
                        i + 1,
                        trusted.get(i).workId(),
                        trusted.get(i).reason(),
                        trusted.get(i).matchedClues() == null ? List.of() : trusted.get(i).matchedClues()
                ))
                .toList();
    }
}
