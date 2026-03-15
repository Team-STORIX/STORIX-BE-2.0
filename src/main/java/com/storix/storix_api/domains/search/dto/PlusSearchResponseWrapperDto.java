package com.storix.storix_api.domains.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlusSearchResponseWrapperDto<T> {

    private Slice<T> result;    // 무한 스크롤 데이터

}
