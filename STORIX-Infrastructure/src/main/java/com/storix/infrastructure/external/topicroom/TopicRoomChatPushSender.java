package com.storix.infrastructure.external.topicroom;

import com.storix.domain.domains.topicroom.dto.TopicRoomChatPushTarget;
import com.storix.domain.domains.topicroom.service.TopicRoomChatPushService;
import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.notification.domain.NotificationCategory;
import com.storix.domain.domains.notification.domain.TargetType;
import com.storix.infrastructure.external.notification.dto.PushMessage;
import com.storix.infrastructure.external.notification.fcm.FcmPushExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TopicRoomChatPushSender {

    private static final String PUSH_TYPE = "TOPIC_ROOM_CHAT";
    private static final String THREAD_ID_PREFIX = "topic-room-";

    private final TopicRoomChatPushService topicRoomChatPushService;
    private final FcmPushExecutor fcmPushExecutor;

    public void send(Long roomId, Long afterMessageId, Long upToMessageId,
                     Long senderId, String senderNickname, String lastMessage) {
        List<TopicRoomChatPushTarget> targets =
                topicRoomChatPushService.resolveTargets(roomId, senderId, afterMessageId, upToMessageId);
        if (targets.isEmpty()) {
            log.debug(">>>> [TopicRoomPush] 발송 대상 없음 roomId={}", roomId);
            return;
        }

        String roomName = topicRoomChatPushService.findRoomName(roomId);
        if (roomName == null) {
            log.warn(">>>> [TopicRoomPush] 방 정보 없음 roomId={}", roomId);
            return;
        }

        String senderProfileImageUrl = topicRoomChatPushService.findSenderProfileImageUrl(senderId);

        int maxBatchCount = targets.stream()
                .mapToInt(TopicRoomChatPushTarget::batchMessageCount)
                .max()
                .orElse(0);
        log.info(">>>> [TopicRoomPush] 발송 시작 roomId={}, targets={}, maxBatchCount={}",
                roomId, targets.size(), maxBatchCount);

        // 뱃지와 묶음 개수가 유저마다 달라 payload 는 유저 단위로 만들되, 발송은 한 배치로 묶는다
        List<PushMessage> messages = new ArrayList<>();
        for (TopicRoomChatPushTarget target : targets) {
            Map<String, String> data =
                    buildData(roomId, roomName, senderNickname, senderProfileImageUrl, lastMessage, target);
            target.tokens().forEach(token -> messages.add(new PushMessage(token, data, true)));
        }

        // collapse 하지 않는다 — 최초 개별 알림과 묶음 알림이 각각 남아야 함
        fcmPushExecutor.sendEachWithRetry(messages, null, "roomId=" + roomId + ", tokens=" + messages.size());
    }

    private Map<String, String> buildData(Long roomId,
                                          String roomName,
                                          String senderNickname,
                                          String senderProfileImageUrl,
                                          String lastMessage,
                                          TopicRoomChatPushTarget target) {
        int messageCount = target.batchMessageCount();
        boolean single = messageCount <= 1;

        Map<String, String> data = new HashMap<>();
        data.put("type", PUSH_TYPE);
        data.put("category", NotificationCategory.TOPIC_ROOM.name());
        data.put("targetType", TargetType.TOPIC_ROOM.name());
        data.put("targetId", String.valueOf(roomId));
        data.put("roomName", roomName);
        data.put("senderNickname", senderNickname);
        data.put("senderProfileImageUrl", senderProfileImageUrl);
        data.put("messageCount", String.valueOf(messageCount));
        data.put("unreadCount", String.valueOf(target.badgeCount()));
        data.put("title", single ? senderNickname : "새 메시지 " + messageCount + "건");
        data.put("subtitle", roomName);
        data.put("body", preview(lastMessage));

        // 앱이 알림을 직접 꾸미도록 열어두고, 알림센터에서는 방 단위로 쌓이게
        data.put("mutableContent", "true");
        data.put("threadId", THREAD_ID_PREFIX + roomId);
        return data;
    }

    // 본문 미리보기 — 30자 초과 시 잘라서 '…' 부가
    private String preview(String text) {
        if (text == null) return "";
        int max = STORIXStatic.Notification.CHAT_PREVIEW_MAX;
        if (text.codePointCount(0, text.length()) <= max) return text;

        // 이모지 검증
        return text.substring(0, text.offsetByCodePoints(0, max))
                + STORIXStatic.Notification.CONTENT_PREVIEW_SUFFIX;
    }
}
