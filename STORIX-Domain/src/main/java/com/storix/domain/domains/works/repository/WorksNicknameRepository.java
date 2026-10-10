package com.storix.domain.domains.works.repository;

import com.storix.domain.domains.works.domain.WorksNickname;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WorksNicknameRepository extends JpaRepository<WorksNickname, Long> {

    List<WorksNickname> findByWorksIdIn(Collection<Long> worksIds);

    List<WorksNickname> findByWorksIdOrderByIdAsc(Long worksId);

    Optional<WorksNickname> findByIdAndWorksId(Long id, Long worksId);

    boolean existsByWorksIdAndNormalized(Long worksId, String normalized);

    /** 작품 병합 */
    @Query("SELECT n.normalized FROM WorksNickname n WHERE n.worksId = :worksId")
    List<String> findNormalizedByWorksId(@Param("worksId") Long worksId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM WorksNickname n WHERE n.worksId = :worksId AND n.normalized IN :normalized")
    int deleteByWorksIdAndNormalized(@Param("worksId") Long worksId, @Param("normalized") List<String> normalized);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE WorksNickname e SET e.worksId = :toWorksId WHERE e.worksId = :fromWorksId")
    int moveWorks(@Param("fromWorksId") Long fromWorksId, @Param("toWorksId") Long toWorksId);
}
