package com.storix.domain.domains.works.domain;

import com.storix.domain.domains.hashtag.domain.Hashtag;
import com.storix.domain.domains.works.dto.HashtagRemoveResult;
import com.storix.domain.domains.works.dto.WorksHashtagRemoveResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[작품] 해시태그 제거")
class WorksHashtagRemoveTest {

    @Test
    @DisplayName("이름이 같은 태그만 끊고 나머지는 남긴다")
    void removeHashtags() {
        Works works = Works.builder().worksName("작품").artistName("작가").build();
        works.addHashtags(Set.of(new Hashtag("봄티콘"), new Hashtag("로맨스판타지"), new Hashtag("소설원작")));

        List<String> removed = works.removeHashtags(List.of("소설원작", "봄티콘", "없는태그"));

        assertThat(removed).containsExactly("봄티콘", "소설원작");
        assertThat(works.getHashtags()).extracting(Hashtag::getName).containsExactly("로맨스판타지");
    }

    @Test
    @DisplayName("붙어 있지 않던 이름은 notFound 로 돌려준다")
    void worksResult() {
        WorksHashtagRemoveResult result = WorksHashtagRemoveResult.of(1L, List.of("봄티콘", "없는태그"), List.of("봄티콘"));

        assertThat(result.removed()).containsExactly("봄티콘");
        assertThat(result.notFound()).containsExactly("없는태그");
    }

    @Test
    @DisplayName("일괄 제거 결과는 요청한 이름마다 작품 수를 담고 없는 이름은 0건이다")
    void bulkResult() {
        HashtagRemoveResult result = HashtagRemoveResult.of(true, List.of("봄티콘", "없는태그"), Map.of("봄티콘", List.of(15946L, 15950L)));

        assertThat(result.dryRun()).isTrue();
        assertThat(result.affected()).extracting(HashtagRemoveResult.Affected::count).containsExactly(2, 0);
        assertThat(result.affected().get(1).worksIds()).isEmpty();
    }
}
