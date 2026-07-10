package com.storix.domain.domains.notification.repository;

import com.storix.domain.domains.notification.domain.Notification;
import com.storix.domain.domains.notification.dto.UnreadCountByUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 최초 조회용
    Slice<Notification> findAllByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    // cursorId 보다 과거 알림 조회
    Slice<Notification> findAllByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long id, Pageable pageable);

    // 안 읽은 알림 개수
    int countByUserIdAndIsReadFalse(Long userId);

    // 여러 유저의 미읽음 수 일괄 조회
    @Query("SELECT new com.storix.domain.domains.notification.dto.UnreadCountByUser(n.userId, COUNT(n)) " +
            "FROM Notification n WHERE n.userId IN :userIds AND n.isRead = false GROUP BY n.userId")
    List<UnreadCountByUser> countUnreadByUserIds(@Param("userIds") List<Long> userIds);

    // 전체 읽음 처리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    void bulkMarkAsRead(@Param("userId") Long userId);
}
