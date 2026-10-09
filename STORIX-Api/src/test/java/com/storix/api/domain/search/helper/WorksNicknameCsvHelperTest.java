package com.storix.api.domain.search.helper;

import com.storix.domain.domains.search.dto.WorksNicknameEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[검색] 작품 별칭 CSV 읽기")
class WorksNicknameCsvHelperTest {

    private final WorksNicknameCsvHelper helper = new WorksNicknameCsvHelper();

    private List<WorksNicknameEntry> parse(String csv) {
        return helper.parse(new MockMultipartFile("file", "nicknames.csv", "text/csv", csv.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("BOM 과 헤더를 건너뛰고 세 번째 칸은 읽지 않는다")
    void skipBomAndHeader() {
        List<WorksNicknameEntry> entries = parse("﻿works_id,nickname,works_name\n4299,나혼렙,나 혼자만 레벨업\n2782,전독시\n");

        assertThat(entries).containsExactly(
                new WorksNicknameEntry(4299L, "나혼렙"),
                new WorksNicknameEntry(2782L, "전독시"));
    }

    @Test
    @DisplayName("헤더가 없으면 첫 줄부터 읽는다")
    void noHeader() {
        assertThat(parse("13,화귀")).containsExactly(new WorksNicknameEntry(13L, "화귀"));
    }

    @Test
    @DisplayName("따옴표로 감싼 칸은 쉼표가 있어도 한 칸으로 읽는다")
    void quotedColumn() {
        assertThat(parse("works_id,nickname,works_name\n1,\"별칭, 쉼표\",\"작품 \"\"이름\"\"\"\n"))
                .containsExactly(new WorksNicknameEntry(1L, "별칭, 쉼표"));
    }

    @Test
    @DisplayName("작품 ID 가 숫자가 아니면 worksId 를 비워 둔다")
    void invalidWorksId() {
        assertThat(parse("works_id,nickname\nabc,별칭\n\n5\n"))
                .containsExactly(new WorksNicknameEntry(null, "별칭"), new WorksNicknameEntry(5L, null));
    }
}
