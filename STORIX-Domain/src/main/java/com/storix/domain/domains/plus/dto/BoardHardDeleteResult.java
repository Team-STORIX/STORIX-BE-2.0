package com.storix.domain.domains.plus.dto;

// 게시글 하드 삭제 배치 결과 (삭제된 게시글 수 + S3 정리 대상 이미지 수)
public record BoardHardDeleteResult(
        int boardCount,
        int imageCount
) {
}
