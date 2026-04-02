package com.storix.domain.domains.works.repository;

import com.storix.domain.domains.works.domain.Genre;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksSortType;
import com.storix.domain.domains.works.domain.WorksType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;

public interface WorksRepositoryCustom {

    Slice<Works> searchWithFilters(
            String keyword,
            List<WorksType> worksTypes,
            List<Genre> genres,
            WorksSortType sortType,
            Pageable pageable
    );
}
