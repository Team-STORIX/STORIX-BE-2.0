package com.storix.domain.domains.search.service;

import static com.storix.common.utils.RedisKeyStatic.Search.FEEDS_REINDEX_LOCK;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.storix.domain.domains.feed.adaptor.ReaderFeedAdaptor;
import com.storix.domain.domains.plus.domain.ReaderBoard;
import com.storix.domain.domains.search.config.FeedIndexProperties;
import com.storix.domain.domains.search.dto.FeedDocument;
import com.storix.domain.domains.search.dto.SearchReindexResponse;
import com.storix.domain.domains.search.exception.SearchReindexInProgressException;
import com.storix.domain.domains.works.adaptor.WorksAdaptor;
import com.storix.domain.domains.works.dto.WorksInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedIndexService {

    private static final String INDEX_DEFINITION = "elasticsearch/feeds-index.json";
    private static final int CHUNK_SIZE = 500;

    private final ElasticsearchClient client;
    private final SearchIndexManager searchIndexManager;
    private final FeedIndexProperties feedIndexProperties;
    private final ReaderFeedAdaptor readerFeedAdaptor;
    private final WorksAdaptor worksAdaptor;

    public boolean isAliasMissing() throws IOException {
        return searchIndexManager.isAliasMissing(feedIndexProperties.alias());
    }

    // 재색인 도중 바뀐 별칭은 새 인덱스에 빠질 수 있어 그동안은 별칭을 못 바꾸게 한다
    public void checkNotReindexing() {
        if (searchIndexManager.isReindexing(FEEDS_REINDEX_LOCK)) throw SearchReindexInProgressException.EXCEPTION;
    }

    public void indexBoard(Long boardId) {
        try {
            Optional<ReaderBoard> board = readerFeedAdaptor.findActiveBoard(boardId);
            if (board.isEmpty()) return;

            FeedDocument document = toDocuments(List.of(board.get())).get(0);
            // alias 가 아직 없을 때 같은 이름의 일반 인덱스가 자동 생성되면 재색인이 alias 를 못 붙인다
            client.index(i -> i.index(feedIndexProperties.alias()).id(String.valueOf(boardId)).document(document).requireAlias(true));
        } catch (Exception e) {
            log.warn(">>> [FeedIndex] 게시글 색인 실패 boardId={}, cause={}", boardId, e.getMessage());
        }
    }

    public void deleteBoard(Long boardId) {
        try {
            client.delete(d -> d.index(feedIndexProperties.alias()).id(String.valueOf(boardId)));
        } catch (Exception e) {
            log.warn(">>> [FeedIndex] 게시글 색인 삭제 실패 boardId={}, cause={}", boardId, e.getMessage());
        }
    }

    // 별칭이 바뀐 작품의 게시글 다시 색인. 실패해도 다음 재색인 때 맞춰진다
    public void indexBoardsOfWorks(Collection<Long> worksIds) {
        long lastBoardId = 0;
        try {
            while (true) {
                List<ReaderBoard> chunk = readerFeedAdaptor.findActiveBoardsOfWorksAfter(worksIds, lastBoardId, CHUNK_SIZE);
                if (chunk.isEmpty()) return;

                searchIndexManager.bulkIndex(feedIndexProperties.alias(), toDocuments(chunk), document -> String.valueOf(document.boardId()));
                lastBoardId = chunk.get(chunk.size() - 1).getId();
            }
        } catch (Exception e) {
            log.warn(">>> [FeedIndex] 작품 게시글 색인 실패 worksIds={}, cause={}", worksIds, e.getMessage());
        }
    }

    public SearchReindexResponse reindexAll() {
        return searchIndexManager.reindex(feedIndexProperties.alias(), INDEX_DEFINITION, FEEDS_REINDEX_LOCK, this::indexAllBoards);
    }

    private long indexAllBoards(String index) throws IOException {
        long count = 0;
        long lastBoardId = 0;

        while (true) {
            List<ReaderBoard> chunk = readerFeedAdaptor.findActiveBoardsAfter(lastBoardId, CHUNK_SIZE);
            if (chunk.isEmpty()) return count;

            searchIndexManager.bulkIndex(index, toDocuments(chunk), document -> String.valueOf(document.boardId()));

            count += chunk.size();
            lastBoardId = chunk.get(chunk.size() - 1).getId();
        }
    }

    private List<FeedDocument> toDocuments(List<ReaderBoard> boards) {
        List<Long> worksIds = boards.stream()
                .map(ReaderBoard::getWorksId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, WorksInfo> works = worksAdaptor.findAllWorksInfoByWorksIds(worksIds);
        Map<Long, List<String>> nicknames = worksAdaptor.loadNicknamesByWorksIds(worksIds);

        return boards.stream()
                .map(board -> {
                    WorksInfo info = board.getWorksId() == null ? null : works.get(board.getWorksId());
                    return FeedDocument.of(
                            board,
                            info == null ? null : info.worksName(),
                            board.getWorksId() == null ? List.of() : nicknames.getOrDefault(board.getWorksId(), List.of()));
                })
                .toList();
    }
}
