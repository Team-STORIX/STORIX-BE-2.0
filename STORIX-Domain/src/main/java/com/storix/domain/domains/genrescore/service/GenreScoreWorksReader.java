package com.storix.domain.domains.genrescore.service;

import com.storix.domain.domains.works.application.port.LoadWorksPort;
import com.storix.domain.domains.works.domain.Works;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreScoreWorksReader {

    private final LoadWorksPort loadWorksPort;

    // 작품 데이터가 깨져 있으면 조회가 예외를 던져 호출자 트랜잭션까지 롤백된다. 따로 떼어 읽는다
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Works findById(Long worksId) {
        return loadWorksPort.findById(worksId);
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public List<Works> findWorksByIds(List<Long> worksIds) {
        return loadWorksPort.findWorksByIds(worksIds);
    }
}
