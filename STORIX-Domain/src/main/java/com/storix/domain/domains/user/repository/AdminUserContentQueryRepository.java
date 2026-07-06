package com.storix.domain.domains.user.repository;

import com.storix.domain.domains.report.domain.TargetContentType;
import com.storix.domain.domains.user.dto.AdminUserContentKey;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AdminUserContentQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<AdminUserContentKey> findContentKeys(Long userId, Pageable pageable) {
        Query query = entityManager.createNativeQuery("""
                SELECT content_type, content_id, created_at
                FROM (
                    SELECT 'FEED' AS content_type, rb.reader_board_id AS content_id, rb.created_at AS created_at
                    FROM reader_board rb
                    WHERE rb.user_id = :userId AND rb.deleted = false
                    UNION ALL
                    SELECT 'FEED_REPLY' AS content_type, r.id AS content_id, r.created_at AS created_at
                    FROM reader_board_reply r
                    WHERE r.user_id = :userId AND r.deleted = false
                    UNION ALL
                    SELECT 'CHAT' AS content_type, m.id AS content_id, m.created_at AS created_at
                    FROM chat_message m
                    WHERE m.sender_id = :userId AND m.deleted = false
                    UNION ALL
                    SELECT 'REVIEW' AS content_type, rv.review_id AS content_id, rv.created_at AS created_at
                    FROM review rv
                    WHERE rv.library_user_id = :userId AND rv.deleted = false
                ) user_contents
                ORDER BY created_at DESC, content_type ASC, content_id DESC
                """);
        query.setParameter("userId", userId);
        query.setFirstResult(Math.toIntExact(pageable.getOffset()));
        query.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        return rows.stream()
                .map(row -> new AdminUserContentKey(
                        TargetContentType.valueOf((String) row[0]),
                        ((Number) row[1]).longValue(),
                        toLocalDateTime(row[2])
                ))
                .toList();
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        return (LocalDateTime) value;
    }
}
