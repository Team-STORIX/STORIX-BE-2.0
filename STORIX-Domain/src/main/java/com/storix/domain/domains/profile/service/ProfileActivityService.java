package com.storix.domain.domains.profile.service;

import com.storix.domain.domains.feed.domain.ReaderBoardReply;
import com.storix.domain.domains.feed.dto.ReaderBoardReplyInfoWithProfile;
import com.storix.domain.domains.plus.service.ReaderBoardHelper;
import com.storix.domain.domains.plus.dto.ReaderBoardInfo;
import com.storix.domain.domains.profile.dto.ReaderBoardWithProfileInfo;
import com.storix.domain.domains.user.adaptor.UserAdaptor;
import com.storix.domain.domains.user.dto.StandardProfileInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProfileActivityService {

    private final UserAdaptor userAdaptor;

    private final ReaderBoardHelper readerBoardHelper;

    // 댓글 조회
    // 좋아요 조회

    // 내가 쓴 게시글 리스트 조회
    @Transactional(readOnly = true)
    public Slice<ReaderBoardWithProfileInfo> findAllReaderBoardList(Long userId, Pageable pageable) {

        // 1) 내 프로필 정보
        StandardProfileInfo profileInfo =
                userAdaptor.findStandardProfileInfoByUserId(userId);

        // 2) 내 게시글 정보
        Slice<ReaderBoardInfo> boards =
                readerBoardHelper.findReaderBoardInfo(userId, null, pageable);

        return readerBoardHelper.map(boards, boardInfo -> profileInfo);
    }

    // 내가 쓴 댓글 리스트 조회
    @Transactional(readOnly = true)
    public Slice<ReaderBoardReplyInfoWithProfile> findAllReaderBoardReplyList(Long userId, Pageable pageable) {

        // 1) 내 프로필 정보
        StandardProfileInfo profileInfo =
                userAdaptor.findStandardProfileInfoByUserId(userId);

        // 2) 내 댓글 정보
        Slice<ReaderBoardReply> replies =
                readerBoardHelper.findReaderBoardReplyInfo(userId, pageable);

        return readerBoardHelper.mapMyRepliesWithProfileAndLike(userId, profileInfo, replies);
    }

    // 내가 누른 좋아요 게시글 리스트 조회
    @Transactional(readOnly = true)
    public Slice<ReaderBoardWithProfileInfo> findAllReaderBoardsLikeList(Long userId, Pageable pageable) {

        // 1) 좋아요 누른 게시글 정보
        Slice<ReaderBoardInfo> boards =
                readerBoardHelper.findLikedReaderBoardInfo(userId, pageable);

        List<ReaderBoardInfo> content = boards.getContent();
        if (content.isEmpty()) {
            return new SliceImpl<>(List.of(), pageable, boards.hasNext());
        }

        // 2) 유저 id 리스트
        List<Long> userIds = boards.getContent().stream()
                .map(ReaderBoardInfo::userId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // 3) 프로필 정보
        Map<Long, StandardProfileInfo> profileMap =
                userAdaptor.findStandardProfileInfoByUserIds(userIds);

        // 3-1) 프로필이 없을 경우 필터링
        List<ReaderBoardInfo> filtered = content.stream()
                .filter(b -> profileMap.get(b.userId()) != null)
                .toList();

        Slice<ReaderBoardInfo> filteredBoards =
                new SliceImpl<>(filtered, pageable, boards.hasNext());

        // 4) 최종 매핑
        return readerBoardHelper.map(
                filteredBoards,
                boardInfo -> profileMap.get(boardInfo.userId())
        );
    }
}
