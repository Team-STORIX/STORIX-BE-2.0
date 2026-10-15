package com.storix.domain.domains.works.domain;

import com.storix.common.exception.STORIXDynamicException;
import com.storix.domain.domains.works.dto.WorksPlatformRemoveResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[작품] 플랫폼 떼기")
class WorksPlatformRemoveTest {

    private Works works() {
        Works works = Works.builder().worksName("작품").artistName("작가").build();
        works.addPlatform(Platform.NAVER_NOVEL, null);
        works.addPlatform(Platform.NAVER_SERIES, "https://series.naver.com/novel/detail.series?productNo=1");
        return works;
    }

    @Test
    @DisplayName("요청한 플랫폼만 떼고 나머지는 남긴다")
    void removePlatforms() {
        Works works = works();
        Set<WorksPlatform> before = works.getPlatforms();
        List<Platform> targets = List.of(Platform.NAVER_NOVEL, Platform.KAKAO_PAGE);

        List<WorksPlatform> removed = works.removePlatforms(targets, false);
        WorksPlatformRemoveResult result = WorksPlatformRemoveResult.of(1L, false, targets, before, removed);

        assertThat(works.getPlatforms()).extracting(WorksPlatform::getPlatform).containsExactly(Platform.NAVER_SERIES);
        assertThat(result.removed()).extracting(WorksPlatformRemoveResult.Removed::platform).containsExactly(Platform.NAVER_NOVEL);
        assertThat(result.notFound()).containsExactly(Platform.KAKAO_PAGE);
        assertThat(result.remaining()).containsExactly(Platform.NAVER_SERIES);
    }

    @Test
    @DisplayName("dryRun 이면 떼지 않고 뗄 플랫폼만 돌려준다")
    void dryRun() {
        Works works = works();

        List<WorksPlatform> removed = works.removePlatforms(List.of(Platform.NAVER_NOVEL), true);

        assertThat(removed).extracting(WorksPlatform::getPlatform).containsExactly(Platform.NAVER_NOVEL);
        assertThat(works.getPlatforms()).hasSize(2);
    }

    @Test
    @DisplayName("플랫폼이 하나도 남지 않으면 dryRun 이어도 거절한다")
    void rejectRemovingAll() {
        Works works = works();

        assertThatThrownBy(() -> works.removePlatforms(List.of(Platform.NAVER_NOVEL, Platform.NAVER_SERIES), true))
                .isInstanceOf(STORIXDynamicException.class);
        assertThat(works.getPlatforms()).hasSize(2);
    }
}
