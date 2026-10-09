package com.storix.domain.domains.works.repository;

import com.storix.domain.domains.works.domain.WorksNickname;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WorksNicknameRepository extends JpaRepository<WorksNickname, Long> {

    List<WorksNickname> findByWorksIdIn(Collection<Long> worksIds);

    List<WorksNickname> findByWorksIdOrderByIdAsc(Long worksId);

    Optional<WorksNickname> findByIdAndWorksId(Long id, Long worksId);

    boolean existsByWorksIdAndNormalized(Long worksId, String normalized);
}
