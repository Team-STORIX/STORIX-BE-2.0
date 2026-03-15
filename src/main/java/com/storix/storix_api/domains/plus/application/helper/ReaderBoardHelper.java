package com.storix.storix_api.domains.plus.application.helper;

import com.storix.storix_api.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.storix_api.domains.feed.domain.ReaderBoardReply;
import com.storix.storix_api.domains.feed.dto.ReaderBoardReplyInfo;
import com.storix.storix_api.domains.feed.dto.ReaderBoardReplyInfoWithProfile;
import com.storix.storix_api.domains.feed.dto.StandardReplyInfoWithLike;
import com.storix.storix_api.domains.hashtag.adaptor.HashtagAdaptor;
import com.storix.storix_api.domains.plus.adaptor.BoardAdaptor;
import com.storix.storix_api.domains.plus.adaptor.BoardImageAdaptor;
import com.storix.storix_api.domains.plus.domain.ReaderBoard;
import com.storix.storix_api.domains.plus.dto.ReaderBoardImageInfo;
import com.storix.storix_api.domains.plus.dto.ReaderBoardInfo;
import com.storix.storix_api.domains.plus.dto.StandardReaderBoardInfo;
import com.storix.storix_api.domains.profile.dto.ReaderBoardWithProfileInfo;
import com.storix.storix_api.domains.user.adaptor.UserAdaptor;
import com.storix.storix_api.domains.user.dto.StandardProfileInfo;
import com.storix.storix_api.domains.works.application.port.LoadWorksPort;
import com.storix.storix_api.domains.works.dto.WorksInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ReaderBoardHelper {

    private final UserAdaptor userAdaptor;
    private final BoardAdaptor boardAdaptor;
    private final ReaderFeedAdaptor readerFeedAdaptor;
    private final BoardImageAdaptor boardImageAdaptor;
    private final HashtagAdaptor hashTagAdaptor;

    private final LoadWorksPort loadWorksPort;

    // 게시글 리스트 조회
    public Slice<ReaderBoardInfo> findReaderBoardInfo(Long userId, Long worksId, Pageable pageable) {

        Slice<ReaderBoard> boards;

        // 작품 관련 게시글 조회
        if (worksId != null) {
            // 피드 관련
            boards = boardAdaptor.findAllReaderBoardListByWorksId(worksId, pageable);
        } else {
            // 프로필 관련
            boards = boardAdaptor.findAllReaderBoardList(userId, pageable);
        }

        List<Long> boardIds = boards.getContent().stream()
                .map(ReaderBoard::getId)
                .toList();

        // 좋아요 여부 정보 조회
        Set<Long> likedBoardIds =
                readerFeedAdaptor.findLikedBoardIds(userId, boardIds);

        // 작품 정보 매핑
        if (worksId != null) {
            // 피드 관련
            return boards.map(board ->
                    ReaderBoardInfo.ofFeedBoard(
                            board,
                            likedBoardIds.contains(board.getId())
                    )
            );
        } else {
            // 프로필 관련
            return boards.map(board ->
                    ReaderBoardInfo.ofMyBoard(
                            board,
                            likedBoardIds.contains(board.getId())
                    )
            );
        }

    }

    // 게시글 단건 조회
    public ReaderBoardInfo findSingleReaderBoardInfo(Long userId, Long boardId, boolean isFeed) {
        ReaderBoard board = boardAdaptor.findReaderBoard(boardId);

        boolean isLiked = false;
        if (userId != null) {
            isLiked = readerFeedAdaptor.isBoardLiked(userId, boardId);
        }

        return isFeed
                ? ReaderBoardInfo.ofFeedBoard(board, isLiked)
                : ReaderBoardInfo.ofMyBoard(board, isLiked);
    }

    // 좋아요 누른 게시글 조회
    public Slice<ReaderBoardInfo> findLikedReaderBoardInfo(Long userId, Pageable pageable) {

        Slice<ReaderBoard> boardsEntity =
                readerFeedAdaptor.findAllLikedReaderBoards(userId, pageable);

        return boardsEntity.map(board ->
                ReaderBoardInfo.ofFeedBoard(board, true)
        );
    }

    // 오늘의 피드 조회
    public List<ReaderBoardInfo> findTop3TrendingFeedInfo(Long userId, LocalDateTime threshold) {

        // 1) 오늘의 피드 (최대 3개)
        List<StandardReaderBoardInfo> boards = readerFeedAdaptor.findTop3TrendingFeed(threshold);

        if (boards.size() < 3) {
            int needed = 3 - boards.size();

            // 오늘의 피드 게시글 Ids
            List<Long> excludeIds = boards.stream()
                    .map(StandardReaderBoardInfo::boardId)
                    .toList();

            // 부족한 개수만큼만 전체 인기순 적용 (최근 7일까지)
            List<StandardReaderBoardInfo> fallbackBoards = readerFeedAdaptor.findSteadyTrendingFeedNotToday(excludeIds, needed);

            boards.addAll(fallbackBoards);
        }

        // 인기 점수 기준 재정렬
        boards.sort(
                Comparator
                        .comparing(StandardReaderBoardInfo::popularityScore)
                        .reversed()
        );

        // 상위 3개 유지
        boards = boards.stream()
                .limit(3)
                .toList();

        // 게시물 ids
        List<Long> boardIds = boards.stream()
                .map(StandardReaderBoardInfo::boardId)
                .toList();

        // 2) 좋아요 여부 조회 - 비로그인 유저의 경우 empty
        Set<Long> likedBoardIds = (userId != null && !boardIds.isEmpty())
                ? readerFeedAdaptor.findLikedBoardIds(userId, boardIds)
                : Collections.emptySet();


        // 최종 매핑
        return boards.stream()
                .map(board -> ReaderBoardInfo.ofHomeBoard(
                        board,
                        likedBoardIds.contains(board.boardId())
                ))
                .toList();
    }

    public Slice<ReaderBoardWithProfileInfo> map(
            Slice<ReaderBoardInfo> boards,
            Function<ReaderBoardInfo, StandardProfileInfo> profileResolver
    ) {
        List<ReaderBoardInfo> content = boards.getContent();
        if (content.isEmpty()) return boards.map(b -> null);

        // 게시글 id 리스트
        List<Long> boardIds = content.stream()
                .map(ReaderBoardInfo::boardId)
                .toList();

        // 1) 게시글 이미지 매핑
        Map<Long, List<ReaderBoardImageInfo>> imageMap =
                boardImageAdaptor.findReaderBoardImagesByBoardIds(boardIds);

        // 작품 id 리스트
        List<Long> worksIds = content.stream()
                .map(ReaderBoardInfo::worksId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 2) 게시글 작품 정보 매핑
        Map<Long, WorksInfo> worksMap = worksIds.isEmpty()
                ? Collections.emptyMap()
                : loadWorksPort.findAllWorksInfoByWorksIds(worksIds);

        // 3) 게시글 작품 해시태그 정보 매핑
        Map<Long, List<String>> hashtagMap = worksIds.isEmpty()
                ? Collections.emptyMap()
                : hashTagAdaptor.findHashTagsByWorksIds(worksIds);

        // 최종 매핑
        return boards.map(boardInfo -> {
            StandardProfileInfo profile = profileResolver.apply(boardInfo);

            Long worksId = boardInfo.worksId();
            return ReaderBoardWithProfileInfo.of(
                    profile,
                    boardInfo,
                    imageMap.getOrDefault(boardInfo.boardId(), List.of()),
                    worksId == null ? null : worksMap.get(worksId),
                    worksId == null ? List.of() : hashtagMap.getOrDefault(worksId, List.of())
            );
        });
    }

    public ReaderBoardWithProfileInfo mapSingle(
            ReaderBoardInfo boardInfo,
            StandardProfileInfo profile
    ) {
        Long boardId = boardInfo.boardId();

        // 1) 이미지 매핑
        Map<Long, List<ReaderBoardImageInfo>> imageMap =
                boardImageAdaptor.findReaderBoardImagesByBoardIds(List.of(boardId));

        // 2) 작품 정보
        Long worksId = boardInfo.worksId();

        WorksInfo works = null;
        List<String> hashtags = List.of();

        if (worksId != null) {
            works = loadWorksPort.findAllWorksInfoByWorksIds(List.of(worksId)).get(worksId);
            hashtags = hashTagAdaptor.findHashTagsByWorksIds(List.of(worksId))
                    .getOrDefault(worksId, List.of());
        }

        return ReaderBoardWithProfileInfo.of(
                profile,
                boardInfo,
                imageMap.getOrDefault(boardId, List.of()),
                works,
                hashtags
        );
    }

    // 댓글 리스트 조회
    public Slice<ReaderBoardReply> findReaderBoardReplyInfo(Long userId, Pageable pageable) {
        return readerFeedAdaptor.findAllByUserId(userId, pageable);
    }

    public Slice<ReaderBoardReplyInfoWithProfile> mapRepliesWithProfileAndLike(
            Long userId,
            Slice<ReaderBoardReply> replies
    ) {
        List<ReaderBoardReply> content = replies.getContent();
        if (content.isEmpty()) {
            return replies.map(r -> null);
        }

        // 1) 댓글 작성자 userIds
        List<Long> userIds = content.stream()
                .map(ReaderBoardReply::getUserId)
                .distinct()
                .toList();

        // 2) 프로필 조회
        Map<Long, StandardProfileInfo> profileMap =
                userAdaptor.findStandardProfileInfoByUserIds(userIds);

        // 3) 댓글 ids
        List<Long> replyIds = content.stream()
                .map(ReaderBoardReply::getId)
                .toList();

        // 4) 좋아요 여부 조회
        Set<Long> likedReplyIds = (userId != null)
                ? readerFeedAdaptor.findLikedReplyIds(userId, replyIds)
                : Collections.emptySet();

        // 5) 최종 매핑
        return replies.map(reply -> {
            StandardProfileInfo profile = profileMap.get(reply.getUserId());
            if (profile == null) return null;

            ReaderBoardReplyInfo replyInfo = ReaderBoardReplyInfo.from(reply);
            boolean isLiked = likedReplyIds.contains(reply.getId());

            return ReaderBoardReplyInfoWithProfile.of(
                    profile,
                    StandardReplyInfoWithLike.of(replyInfo, isLiked)
            );
        });
    }

    public Slice<ReaderBoardReplyInfoWithProfile> mapMyRepliesWithProfileAndLike(
            Long userId,
            StandardProfileInfo myProfile,
            Slice<ReaderBoardReply> replies
    ) {
        List<ReaderBoardReply> content = replies.getContent();
        if (content.isEmpty()) {
            return new SliceImpl<>(List.of(), replies.getPageable(), replies.hasNext());
        }

        // 1) 댓글 ids
        List<Long> replyIds = content.stream()
                .map(ReaderBoardReply::getId)
                .toList();

        // 2) 좋아요 여부 조회
        Set<Long> likedReplyIds = (userId != null)
                ? readerFeedAdaptor.findLikedReplyIds(userId, replyIds)
                : Collections.emptySet();

        // 3) 최종 매핑
        return replies.map(reply -> {
            ReaderBoardReplyInfo replyInfo = ReaderBoardReplyInfo.from(reply);
            boolean isLiked = likedReplyIds.contains(reply.getId());

            return ReaderBoardReplyInfoWithProfile.of(
                    myProfile,
                    StandardReplyInfoWithLike.of(replyInfo, isLiked)
            );
        });
    }

}
