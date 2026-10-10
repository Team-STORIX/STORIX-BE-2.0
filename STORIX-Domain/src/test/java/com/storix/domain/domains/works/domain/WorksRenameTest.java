package com.storix.domain.domains.works.domain;

import com.storix.common.code.ErrorCode;
import com.storix.common.exception.STORIXDynamicException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[작품] 작품명 변경")
class WorksRenameTest {

    @Test
    @DisplayName("현재 이름이 맞으면 이름과 비교용 제목을 같이 바꾼다")
    void rename() {
        Works works = Works.builder().worksName("19절대역").artistName("교결").build();

        works.rename("19절대역", "절대역");

        assertThat(works.getWorksName()).isEqualTo("절대역");
        assertThat(works.getNormalizedName()).isEqualTo(WorksIdentity.titleKey("절대역"));
    }

    @Test
    @DisplayName("현재 이름이 다르면 거절하고 지금 이름을 알려준다")
    void currentNameMismatch() {
        Works works = Works.builder().worksName("절대역").artistName("교결").build();

        assertThatThrownBy(() -> works.rename("19절대역", "절대역"))
                .isInstanceOf(STORIXDynamicException.class)
                .satisfies(e -> {
                    STORIXDynamicException exception = (STORIXDynamicException) e;
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.WORKS_RENAME_CONFLICT);
                    assertThat(exception.getMessage()).contains("절대역");
                });
        assertThat(works.getWorksName()).isEqualTo("절대역");
    }
}
