package com.storix.domain.domains.works.service;

import com.storix.domain.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.domain.domains.genrescore.adaptor.GenreScoreAdaptor;
import com.storix.domain.domains.plus.adaptor.BoardAdaptor;
import com.storix.domain.domains.plus.adaptor.ReviewAdaptor;
import com.storix.domain.domains.plus.domain.Rating;
import com.storix.domain.domains.preference.adaptor.ExplorationAdaptor;
import com.storix.domain.domains.topicroom.adaptor.TopicRoomAdaptor;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksMergePolicy;
import com.storix.domain.domains.works.domain.WorksMergeTarget;
import com.storix.domain.domains.works.dto.WorksMergeCandidate;
import com.storix.domain.domains.works.dto.WorksMergeCounts;
import com.storix.domain.domains.works.dto.WorksMergeResult;
import com.storix.domain.domains.works.dto.WorksMoveCount;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class WorksMergeService {

    private final WorksAdaptor worksAdaptor;
    private final FavoriteWorksAdaptor favoriteWorksAdaptor;
    private final GenreScoreAdaptor genreScoreAdaptor;
    private final ExplorationAdaptor explorationAdaptor;
    private final ReviewAdaptor reviewAdaptor;
    private final BoardAdaptor boardAdaptor;
    private final TopicRoomAdaptor topicRoomAdaptor;

    // 묶음 하나가 트랜잭션 하나. 거절 조건이 하나라도 있으면 전부 되돌림
    @Transactional
    public WorksMergeResult merge(Long keepWorksId, List<Long> dropWorksIds) {
        // 1. 요청 검증
        WorksMergePolicy.checkRequest(keepWorksId, dropWorksIds);
        worksAdaptor.findById(keepWorksId);
        dropWorksIds.forEach(worksAdaptor::findById);

        // 2. 거절 조건: 같은 사용자 리뷰, 둘 다 있는 토픽룸
        List<WorksMergeCandidate> candidates = Stream.concat(Stream.of(keepWorksId), dropWorksIds.stream())
                .map(worksId -> new WorksMergeCandidate(worksId,
                        reviewAdaptor.findLibraryUserIdsByWorksId(worksId), topicRoomAdaptor.existsByWorksId(worksId)))
                .toList();
        WorksMergePolicy.checkConflicts(candidates.get(0), candidates.subList(1, candidates.size()));

        // 3. works_id 참조 옮기기
        WorksMergeCounts counts = WorksMergeCounts.empty();
        for (Long dropWorksId : dropWorksIds) {
            WorksMoveCount favorites = favoriteWorksAdaptor.moveWorks(dropWorksId, keepWorksId);

            counts.add(WorksMergeTarget.WORKS_NICKNAME, worksAdaptor.moveNicknames(dropWorksId, keepWorksId));
            counts.add(WorksMergeTarget.FAVORITE_WORKS, favorites);
            counts.add(WorksMergeTarget.GENRE_SCORE_LOG, genreScoreAdaptor.moveScoreLogs(dropWorksId, keepWorksId, favorites.duplicatedUserIds()));
            counts.add(WorksMergeTarget.TASTE_EXPLORATION, explorationAdaptor.moveWorks(dropWorksId, keepWorksId));
            counts.add(WorksMergeTarget.REVIEW, reviewAdaptor.moveWorks(dropWorksId, keepWorksId));
            counts.add(WorksMergeTarget.READER_BOARD, boardAdaptor.moveReaderBoards(dropWorksId, keepWorksId));
            counts.add(WorksMergeTarget.TOPIC_ROOM, topicRoomAdaptor.moveWorks(dropWorksId, keepWorksId));
        }

        // 4. 작품 정보 · 플랫폼 · 해시태그 합치고 drop 삭제. 벌크 쿼리가 영속성 컨텍스트를 비워 여기서 다시 읽음
        Works keep = worksAdaptor.findById(keepWorksId);
        boolean wasOnboarding = Boolean.TRUE.equals(keep.getIsOnboarding());
        for (Long dropWorksId : dropWorksIds) {
            Works drop = worksAdaptor.findById(dropWorksId);
            keep.mergeFrom(drop);
            worksAdaptor.delete(drop);
        }

        // 5. 리뷰 수 · 평균 별점 다시 계산
        List<Rating> ratings = reviewAdaptor.findActiveRatingsByWorksId(keepWorksId);
        double avgRating = ratings.stream().mapToDouble(Rating::getRatingValue).average().orElse(0.0);
        keep.applyReviewStats(ratings.size(), avgRating);

        return new WorksMergeResult(keepWorksId, dropWorksIds, counts.moved(), counts.removedDuplicates(),
                ratings.size(), avgRating, !wasOnboarding && Boolean.TRUE.equals(keep.getIsOnboarding()));
    }
}
