package com.storix.api.config.swagger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.Operation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.core.io.ClassPathResource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class SwaggerErrorSpecConfigTest {

    private static final String KEY = "ProfileController#updateNickName/2";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private SwaggerErrorSpecConfig config;
    private JsonNode expected;

    /** 실제 컨트롤러를 띄우지 않고 이름만 맞춘 대역. 매칭 키가 클래스명#메서드명이라 이걸로 충분하다. */
    static class ProfileController {
        /** 파라미터 개수까지 맞춰야 키가 같아진다. 예시 필드는 @RequestBody 타입에서 뽑으므로 DTO 도 흉내낸다. */
        public void updateNickName(Object authUserDetails, @RequestBody NickNameRequest request) {}
    }

    record NickNameRequest(@NotBlank(message = "닉네임을 입력해주세요.") String nickName) {}

    @BeforeEach
    void setUp() throws Exception {
        ClassPathResource resource = new ClassPathResource("swagger/error-map.json");
        // node 가 없으면 generateErrorMap 이 건너뛰므로 생성물도 없다. 그때는 검증할 대상이 없다
        assumeTrue(resource.exists(), "에러 스펙 생성물이 없어 건너뜁니다 (generateErrorMap 미실행)");

        config = new SwaggerErrorSpecConfig(objectMapper);
        try (InputStream in = resource.getInputStream()) {
            expected = objectMapper.readTree(in).path("endpoints").path(KEY);
        }
    }

    private Operation customized() throws Exception {
        OperationCustomizer customizer = config.errorResponseCustomizer();
        HandlerMethod handlerMethod =
                new HandlerMethod(new ProfileController(), ProfileController.class.getMethod("updateNickName", Object.class, NickNameRequest.class));
        return customizer.customize(new Operation(), handlerMethod);
    }

    @Test
    @DisplayName("에러 맵에 있는 코드가 상태코드별 예시로 모두 들어간다")
    void 에러코드가_상태별_예시로_들어간다() throws Exception {
        assertThat(expected.isMissingNode()).isFalse();

        Operation operation = customized();

        for (JsonNode side : List.of(expected.path("own"), expected.path("common"))) {
            for (JsonNode entry : side) {
                // 상단 표로 올라간 것들은 개별 응답에 싣지 않는다
                String scope = entry.path("scope").asText("");
                if (scope.equals("auth") || scope.equals("global")) continue;

                String status = String.valueOf(entry.path("status").asInt());
                String code = entry.path("code").asText();

                ApiResponse response = operation.getResponses().get(status);
                assertThat(response).as("%s 응답", status).isNotNull();

                Map<String, Example> examples =
                        response.getContent().get("application/json").getExamples();
                assertThat(examples).containsKey(code);
                assertThat(((Map<?, ?>) examples.get(code).getValue()).get("message"))
                        .isEqualTo(entry.path("message").asText());
            }
        }
    }

    @Test
    @DisplayName("같은 상태코드에 상황이 여러 개면 예시도 여러 개 남는다")
    void 한_상태코드에_예시가_여러개_남는다() throws Exception {
        Operation operation = customized();

        Map<String, Example> badRequest =
                operation.getResponses().get("400").getContent().get("application/json").getExamples();

        assertThat(badRequest).isNotEmpty();
        assertThat(operation.getResponses().get("400").getDescription())
                .contains("code 로 구분해주세요")
                .contains("NICKNAME_ERROR");
    }

    @Test
    @DisplayName("인증 계열은 개별 응답에 싣지 않고 문서 상단에 한 번만 적는다")
    void 인증에러는_상단에만_적는다() throws Exception {
        Operation operation = customized();

        assertThat(operation.getResponses().get("401")).isNull();
        assertThat(operation.getResponses().get("403")).isNull();

        OpenAPI openAPI = new OpenAPI().info(new Info().title("t").description("원래 설명"));
        config.errorSchemaRegistrar().customise(openAPI);

        assertThat(openAPI.getInfo().getDescription())
                .contains("원래 설명")
                .contains("인증 공통 에러")
                .contains("TOKEN_ERROR_001")
                .contains("요청 공통 에러")
                .contains("COMMON_ERROR_008")
                .contains("웹소켓(STOMP) 에러")
                .contains("UNAUTHORIZED");
    }

    @Test
    @DisplayName("검증 실패 예시에는 fieldErrors 가 같이 들어간다")
    void 검증실패는_fieldErrors_까지_보여준다() throws Exception {
        Operation operation = customized();

        Map<String, Example> badRequest =
                operation.getResponses().get("400").getContent().get("application/json").getExamples();

        Map<String, Example> unprocessable =
                operation.getResponses().get("422").getContent().get("application/json").getExamples();
        Map<?, ?> validation = (Map<?, ?>) unprocessable.get("COMMON_ERROR_006").getValue();
        assertThat(validation.containsKey("fieldErrors")).isTrue();

        // 예시 필드는 고정값이 아니라 그 API 의 요청 DTO 에서 나온다
        Map<?, ?> fieldError = (Map<?, ?>) ((List<?>) validation.get("fieldErrors")).get(0);
        assertThat(fieldError.get("field")).isEqualTo("nickName");
        assertThat(fieldError.get("reason")).isEqualTo("NotBlank");
        assertThat(fieldError.get("message")).isEqualTo("닉네임을 입력해주세요.");

        // JSON 형식 오류는 API 성격과 무관해 상단 표로 가고, 개별 응답에는 없다
        assertThat(badRequest).doesNotContainKey("COMMON_ERROR_008");

        Map<?, ?> domain = (Map<?, ?>) badRequest.get("NICKNAME_ERROR_002").getValue();
        assertThat(domain.containsKey("fieldErrors")).isFalse();
    }

    @Test
    @DisplayName("에러 응답 스키마는 components 를 참조한다")
    void 응답_스키마는_ref_로_붙는다() throws Exception {
        Operation operation = customized();

        assertThat(operation.getResponses().get("400").getContent().get("application/json").getSchema().get$ref())
                .isEqualTo("#/components/schemas/ErrorResponse");

        OpenAPI openAPI = new OpenAPI();
        config.errorSchemaRegistrar().customise(openAPI);
        assertThat(openAPI.getComponents().getSchemas()).containsKey("ErrorResponse");
    }

    @Test
    @DisplayName("맵에 없는 핸들러는 건드리지 않는다")
    void 맵에_없으면_그대로_둔다() throws Exception {
        OperationCustomizer customizer = config.errorResponseCustomizer();
        HandlerMethod handlerMethod =
                new HandlerMethod(new Unknown(), Unknown.class.getMethod("nothing"));

        Operation operation = customizer.customize(new Operation(), handlerMethod);

        assertThat(operation.getResponses()).isNull();
    }

    static class Unknown {
        public void nothing() {}
    }
}
