package com.storix.domain.domains.feed.service;

import com.storix.domain.domains.favorite.adaptor.FavoriteWorksAdaptor;
import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.feed.domain.ReaderBoardReply;
import com.storix.domain.domains.feed.dto.BoardWrapperDto;
import com.storix.domain.domains.feed.dto.ReaderBoardReplyInfoWithProfile;
import com.storix.domain.domains.feed.dto.SlicedReaderBoardWithProfileInfo;
import com.storix.domain.domains.plus.service.ReaderBoardHelper;
import com.storix.domain.domains.plus.domain.ReaderBoard;
import com.storix.domain.domains.plus.dto.ReaderBoardInfo;
import com.storix.domain.domains.profile.dto.ReaderBoardWithProfileInfo;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.adaptor.UserBlockAdaptor;
import com.storix.domain.domains.user.exception.block.BlockedUserContentException;
import com.storix.domain.domains.user.dto.StandardProfileInfo;
import com.storix.domain.domains.works.application.port.LoadWorksPort;
import com.storix.domain.domains.works.dto.SlicedWorksInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final UserAdaptor userAdaptor;
    private final UserBlockAdaptor userBlockAdaptor;
    private final FavoriteWorksAdaptor favoriteWorksAdaptor;

    private final ReaderFeedAdaptor readerFeedAdaptor;
    private final ReaderBoardHelper readerBoardHelper;

    private final LoadWorksPort loadWorksPort;

    @Transactional(readOnly = true)
    public Slice<ReaderBoardWithProfileInfo> getAllReaderBoard(Long userId, Pageable pageable) {

        List<Long> blockedIds = userBlockAdaptor.findBlockedUserIds(userId);

        // 1) 최신순 게시글 (차단 유저 제외)
        Slice<ReaderBoard> boards = readerFeedAdaptor.findAllExcludingBlocked(blockedIds, pageable);

        List<Long> boardIds = boards.getContent().stream()
                .map(ReaderBoard::getId)
                .toList();

        // 2) 좋아요 여부
        Set<Long> likedBoardIds = readerFeedAdaptor.findLikedBoardIds(userId, boardIds);


        Slice<ReaderBoardInfo> boardInfos = boards.map(board ->
                ReaderBoardInfo.ofFeedBoard(board, likedBoardIds.contains(board.getId()))
        );

        // 3) 프로필 매핑
        List<Long> writerIds = boardInfos.getContent().stream()
                .map(ReaderBoardInfo::userId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, StandardProfileInfo> profileMap =
                userAdaptor.findStandardProfileInfoByUserIds(writerIds);

        // 최종 매핑
        return readerBoardHelper.map(boardInfos, info ->
                profileMap.get(info.userId()));
    }

    @Transactional(readOnly = true)
    public Slice<SlicedWorksInfo> findFavoriteWorksList(Long userId, Pageable pageable) {

        // 관심 작품 등록 리스트 조회
        Slice<Long> worksIdsSlice = favoriteWorksAdaptor.findSliceFavoriteWorksId(userId, pageable);
        List<Long> worksIds = worksIdsSlice.getContent();

        if (worksIds.isEmpty()) {
            return new SliceImpl<>(List.of(), pageable, worksIdsSlice.hasNext());
        }

        // 1) 관심 작품 정보 조회
        Map<Long, SlicedWorksInfo> slicedWorksInfoMap =
                loadWorksPort.findAllSlicedWorksInfoByWorksIds(worksIds);

        // 최종 매핑
        List<SlicedWorksInfo> result = worksIds.stream()
                .map(slicedWorksInfoMap::get)
                .filter(Objects::nonNull)
                .toList();

        return new SliceImpl<>(result, pageable, worksIdsSlice.hasNext());
    }

    @Transactional(readOnly = true)
    public Slice<ReaderBoardWithProfileInfo> findAllReaderBoardFeedByWorksId(Long userId, Long worksId, Pageable pageable) {

        List<Long> blockedIds = userBlockAdaptor.findBlockedUserIds(userId);

        // 1) 게시글 정보 (차단 유저 제외)
        Slice<ReaderBoardInfo> boards =
                readerBoardHelper.findReaderBoardInfo(userId, worksId, blockedIds, pageable);

        // 유저 id 리스트
        List<Long> userIds = boards.getContent().stream()
                .map(ReaderBoardInfo::userId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 2) 프로필 정보
        Map<Long, StandardProfileInfo> profileMap =
                userAdaptor.findStandardProfileInfoByUserIds(userIds);

        return readerBoardHelper.map(boards, boardInfo ->
                profileMap.get(boardInfo.userId())
        );
    }

    @Transactional(readOnly = true)
    public BoardWrapperDto<ReaderBoardReplyInfoWithProfile> findReaderBoardDetail(Long userId, Long boardId, Pageable replyPageable) {

        List<Long> blockedIds = userBlockAdaptor.findBlockedUserIds(userId);

        // 1) 게시글 단건 정보
        ReaderBoardInfo boardInfo = readerBoardHelper.findSingleReaderBoardInfo(userId, boardId, true);

        if (blockedIds.contains(boardInfo.userId())) {
            throw BlockedUserContentException.EXCEPTION;
        }

        StandardProfileInfo writerProfile =
                userAdaptor.findStandardProfileInfoByUserId(boardInfo.userId());

        ReaderBoardWithProfileInfo board =
                readerBoardHelper.mapSingle(boardInfo, writerProfile);

        // 2) 댓글 정보 (차단 유저의 부모 댓글 제외)
        Slice<ReaderBoardReply> replySlice =
                readerFeedAdaptor.findAllByBoardIdExcludingBlocked(boardId, blockedIds, replyPageable);

        // 3) 차단 유저의 답댓글도 제외
        Slice<ReaderBoardReplyInfoWithProfile> comments =
                readerBoardHelper.mapRepliesWithProfileAndLike(userId, replySlice, blockedIds);

        return new BoardWrapperDto<>(board, comments);
    }


    @Transactional(readOnly = true)
    public List<SlicedReaderBoardWithProfileInfo> findTodayTrendingFeeds(Long userId) {

        // 1) 오늘의 피드 추천
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);

        List<ReaderBoardInfo> boards =
                readerBoardHelper.findTop3TrendingFeedInfo(userId, threshold);

        // 2) 차단 유저 게시글 제외 (캐시된 쿼리라 앱 레벨 필터링)
        Set<Long> blockedSet = userId != null
                ? new java.util.HashSet<>(userBlockAdaptor.findBlockedUserIds(userId))
                : java.util.Collections.emptySet();

        List<ReaderBoardInfo> filtered = boards.stream()
                .filter(board -> !blockedSet.contains(board.userId()))
                .toList();

        // 3) 유저 id 리스트
        List<Long> userIds = filtered.stream()
                .map(ReaderBoardInfo::userId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 4) 프로필 정보
        Map<Long, StandardProfileInfo> profileMap =
                userAdaptor.findStandardProfileInfoByUserIds(userIds);

        // 최종 매핑
        return filtered.stream()
                .map(board -> {
                    StandardProfileInfo profile = profileMap.get(board.userId());
                    if (profile == null) return null;
                    return SlicedReaderBoardWithProfileInfo.of(profile, board);
                })
                .filter(Objects::nonNull)
                .toList();
    }

}
