package com.storix.domain.domains.bannedword.event;

// 금칙어 추가/삭제 트랜잭션 내부에서 발행, 커밋 후 캐시 갱신을 트리거한다
public record BannedWordChangedEvent() {
}
