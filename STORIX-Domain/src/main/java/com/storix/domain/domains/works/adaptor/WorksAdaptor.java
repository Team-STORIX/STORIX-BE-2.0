package com.storix.domain.domains.works.adaptor;

import com.storix.domain.domains.works.repository.WorksRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorksAdaptor {

    private final WorksRepository worksRepository;

    public long countAllWorks() {
        return worksRepository.count();
    }
}
