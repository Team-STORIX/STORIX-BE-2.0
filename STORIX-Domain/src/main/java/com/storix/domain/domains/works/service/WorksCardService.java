package com.storix.domain.domains.works.service;

import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksIdentity;
import com.storix.domain.domains.works.domain.WorksType;
import com.storix.domain.domains.works.dto.WorksCardInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

// AI 작품 검색 카드용 작품 조회
@Service
@RequiredArgsConstructor
public class WorksCardService {

    private final WorksAdaptor worksAdaptor;

    // 없는 작품은 결과에서 빠진다
    @Transactional(readOnly = true)
    public Map<Long, WorksCardInfo> findCards(Collection<Long> worksIds) {
        return worksAdaptor.findWorksByIds(List.copyOf(worksIds)).stream()
                .map(WorksCardInfo::from)
                .collect(Collectors.toMap(WorksCardInfo::worksId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public Optional<Long> findIdByTitle(String worksName, WorksType worksType) {
        return worksAdaptor.findByNormalizedNameAndWorksType(WorksIdentity.titleKey(worksName), worksType).stream()
                .findFirst()
                .map(Works::getId);
    }

    @Transactional(readOnly = true)
    public List<Long> pickRandomIds(List<Long> excludedIds, int needed, boolean excludeAdult) {
        return worksAdaptor.findRandomIdsExcluding(excludedIds, needed, excludeAdult);
    }
}
