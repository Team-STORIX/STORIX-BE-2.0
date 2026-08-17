package com.storix.api.domain.appversion.controller.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.domain.domains.appversion.domain.VersionStatus;
import com.storix.domain.domains.appversion.dto.AppVersionCheck;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[앱 버전] check 응답 직렬화")
class AppVersionCheckResponseTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class));

    @Test
    @DisplayName("배포일은 yy.MM.dd 로 직렬화된다")
    void release_date_is_serialized_as_yy_mm_dd() {
        AppVersionCheckResponse response = AppVersionCheckResponse.from(
                new AppVersionCheck(VersionStatus.UPDATE_AVAILABLE, "1.1.2", "1.0.5",
                        LocalDate.of(2026, 8, 15)));

        contextRunner.run(context -> {
            ObjectMapper objectMapper = context.getBean(ObjectMapper.class);

            assertThat(objectMapper.writeValueAsString(response))
                    .contains("\"releaseDate\":\"26.08.15\"");
        });
    }
}
