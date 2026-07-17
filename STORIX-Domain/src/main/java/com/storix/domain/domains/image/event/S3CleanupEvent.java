package com.storix.domain.domains.image.event;

import java.util.List;

// S3 오브젝트 정리 트리거 이벤트
// 발행 지점 : DB 변경과 같은 트랜잭션 안에서 S3CleanupPublisher.publish() 만 호출했을 경우
// AFTER_COMMIT 시점에 S3CleanupEventListener 가 삭제를 수행 (롤백 시 미실행)
public record S3CleanupEvent(
        List<String> objectKeys
) {
}
