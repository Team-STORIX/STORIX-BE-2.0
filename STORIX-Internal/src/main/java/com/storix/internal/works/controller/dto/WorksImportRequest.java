package com.storix.internal.works.controller.dto;

import com.storix.domain.domains.works.dto.WorksImportItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record WorksImportRequest(
        @Schema(description = "적재할 작품. 한 번에 최대 100건")
        @NotEmpty(message = "적재할 작품이 없습니다.")
        @Size(max = 100, message = "한 번에 100건까지 적재할 수 있습니다.")
        List<@NotNull(message = "작품 항목이 비어 있습니다.") Item> items
) {

    public List<WorksImportItem> toItems() {
        return items.stream()
                .map(item -> new WorksImportItem(item.stagingId(), item.worksName(), item.artistName(),
                        item.author(), item.illustrator(), item.originalAuthor(),
                        item.ageClassification(), item.genre(), item.worksType(), item.platform(), item.landingUrl(),
                        item.description(), item.thumbnailUrl(), item.hashtags(), item.targetWorksId(), Boolean.TRUE.equals(item.createNew())))
                .toList();
    }

    public record Item(
            @Schema(description = "검수 서비스의 staging ID. 결과를 맞춰 보는 데만 쓴다", example = "123")
            Long stagingId,
            @Schema(description = "작품명. 작가 · 작품 유형과 함께 기존 작품 판별 기준", example = "나 혼자만 레벨업")
            String worksName,
            @Schema(description = "전체 작가", example = "추공, 장성락")
            String artistName,
            String author,
            String illustrator,
            String originalAuthor,
            @Schema(description = "enum 이름", example = "AGE_15")
            String ageClassification,
            @Schema(description = "enum 이름", example = "FANTASY")
            String genre,
            @Schema(description = "enum 이름", example = "WEBTOON")
            String worksType,
            @Schema(description = "enum 이름", example = "KAKAO_PAGE")
            String platform,
            @Schema(description = "플랫폼 작품 페이지 링크")
            String landingUrl,
            String description,
            String thumbnailUrl,
            @Schema(description = "기존 해시태그에 더한다. 빼지 않으며 비우면 그대로 둔다")
            List<String> hashtags,
            @Schema(description = "갱신할 기존 작품 id. 있으면 판정 없이 이 작품의 보낸 필드만 갱신한다. 이때 작품명 · 작가 · 작품 유형도 생략할 수 있다", example = "2782")
            Long targetWorksId,
            @Schema(description = "사람이 중복 의심 후보와 다른 작품이라고 판단했으면 true. 중복 의심 판정 없이 새로 만든다", example = "false")
            Boolean createNew
    ) {
    }
}
