package com.storix.domain.domains.works.service;

import com.storix.domain.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.domain.domains.genrescore.adaptor.GenreScoreAdaptor;
import com.storix.domain.domains.plus.adaptor.BoardAdaptor;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.preference.adaptor.ExplorationAdaptor;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.dto.WorksDeleteItem;
import com.storix.domain.domains.works.dto.WorksDeleteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.storix.domain.domains.works.domain.WorksMergeTarget.FAVORITE_WORKS;
import static com.storix.domain.domains.works.domain.WorksMergeTarget.GENRE_SCORE_LOG;
import static com.storix.domain.domains.works.domain.WorksMergeTarget.READER_BOARD;
import static com.storix.domain.domains.works.domain.WorksMergeTarget.REVIEW;
import static com.storix.domain.domains.works.domain.WorksMergeTarget.TASTE_EXPLORATION;
import static com.storix.domain.domains.works.domain.WorksMergeTarget.TOPIC_ROOM;
import static com.storix.domain.domains.works.domain.WorksMergeTarget.WORKS_NICKNAME;
import static com.storix.domain.domains.works.domain.WorksMergeTarget.WORKS_PLATFORM;

@Service
@RequiredArgsConstructor
public class WorksDeleteService {

    private static final String WORKS_HASHTAG = "works_hashtag";

    private final WorksAdaptor worksAdaptor;
    private final FavoriteWorksAdaptor favoriteWorksAdaptor;
    private final GenreScoreAdaptor genreScoreAdaptor;
    private final ExplorationAdaptor explorationAdaptor;
    private final ReviewAdaptor reviewAdaptor;
    private final BoardAdaptor boardAdaptor;
    private final TopicRoomAdaptor topicRoomAdaptor;

    // 요청 하나가 트랜잭션 하나. 막히거나 없는 작품은 건너뛰고, 예상 못 한 오류면 전부 되돌림
    @Transactional
    public WorksDeleteResult deleteAll(List<Long> worksIds, boolean dryRun) {
        List<WorksDeleteItem> items = new ArrayList<>();
        for (Long worksId : worksIds) {
            // 1. 함께 지울 데이터와 삭제를 막는 사용자 콘텐츠 · 활동 기록 세기
            Optional<Works> found = worksAdaptor.findOptionalById(worksId);
            if (found.isEmpty()) {
                items.add(WorksDeleteItem.notFound(worksId));
                continue;
            }
            Works works = found.get();

            Map<String, Long> deleted = new LinkedHashMap<>();
            deleted.put(WORKS_PLATFORM.tableName(), (long) works.getPlatforms().size());
            deleted.put(WORKS_HASHTAG, (long) works.getHashtags().size());
            deleted.put(WORKS_NICKNAME.tableName(), worksAdaptor.countNicknamesByWorksId(worksId));
            deleted.put(TASTE_EXPLORATION.tableName(), explorationAdaptor.countByWorksId(worksId));

            Map<String, Long> userContents = new LinkedHashMap<>();
            userContents.put(REVIEW.tableName(), reviewAdaptor.countAllByWorksId(worksId));
            userContents.put(READER_BOARD.tableName(), boardAdaptor.countReaderBoardsByWorksId(worksId));
            userContents.put(TOPIC_ROOM.tableName(), topicRoomAdaptor.countByWorksId(worksId));
            userContents.put(FAVORITE_WORKS.tableName(), favoriteWorksAdaptor.countByWorksId(worksId));
            userContents.put(GENRE_SCORE_LOG.tableName(), genreScoreAdaptor.countScoreLogsByWorksId(worksId));

            WorksDeleteItem item = WorksDeleteItem.of(worksId, works.getWorksName(), works.getIsOnboarding(), dryRun, deleted, userContents);
            items.add(item);
            if (item.status() != WorksDeleteItem.Status.DELETED) continue;

            // 2. 별칭 · 취향 탐색 지우고 작품 삭제. 플랫폼 · 해시태그 연결은 작품과 함께 지워지고, 벌크 쿼리가 영속성 컨텍스트를 비워 다시 읽음
            worksAdaptor.deleteNicknamesByWorksId(worksId);
            explorationAdaptor.deleteAllByWorksId(worksId);
            worksAdaptor.delete(worksAdaptor.findById(worksId));
        }
        return new WorksDeleteResult(dryRun, items);
    }
}
