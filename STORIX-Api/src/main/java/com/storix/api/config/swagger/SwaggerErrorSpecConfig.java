package com.storix.api.config.swagger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.storix.common.payload.ErrorResponse;
import jakarta.validation.Constraint;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.converter.ResolvedSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.method.HandlerMethod;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 정적 분석으로 뽑아둔 에러 목록을 스웨거 스펙에 붙인다.
 * 컨트롤러에 어노테이션을 달지 않고, 빌드 산출물(error-map.json)만 읽는다.
 */
@Slf4j
@Configuration
public class SwaggerErrorSpecConfig {

    private static final String RESOURCE = "swagger/error-map.json";
    private static final String SCHEMA_NAME = "ErrorResponse";
    private static final String SCHEMA_REF = "#/components/schemas/" + SCHEMA_NAME;
    private static final String SAMPLE_TIMESTAMP = "2026-01-01T00:00:00";
    private static final String CONSTRAINT_PACKAGE = "jakarta.validation.constraints";
    private static final int MAX_FIELD_ERROR_SAMPLES = 12;

    private final Map<String, ErrorSpec> catalog;
    private final WebsocketErrorSpec websocket;

    public SwaggerErrorSpecConfig(ObjectMapper objectMapper) {
        JsonNode root = readSpec(objectMapper);
        this.catalog = load(objectMapper, root);
        this.websocket = objectMapper.convertValue(root.path("_websocket"), WebsocketErrorSpec.class);
    }

    private JsonNode readSpec(ObjectMapper objectMapper) {
        ClassPathResource resource = new ClassPathResource(RESOURCE);
        if (!resource.exists()) {
            log.warn("에러 스펙 없이 기동 resource={}", RESOURCE);
            return objectMapper.createObjectNode();
        }
        try (InputStream in = resource.getInputStream()) {
            return objectMapper.readTree(in);
        } catch (Exception e) {
            log.error("에러 스펙 적재 실패 resource={}", RESOURCE, e);
            return objectMapper.createObjectNode();
        }
    }

    private Map<String, ErrorSpec> load(ObjectMapper objectMapper, JsonNode root) {
        JsonNode endpoints = root.path("endpoints");
        Map<String, ErrorSpec> loaded = new LinkedHashMap<>();
        // 같은 메시지가 엔드포인트마다 새 String 으로 뜬다. 한 벌만 남겨 상주 메모리를 줄인다.
        Map<String, String> pool = new HashMap<>();
        endpoints.fields().forEachRemaining(e ->
                loaded.put(e.getKey(), share(objectMapper.convertValue(e.getValue(), ErrorSpec.class), pool)));
        log.info("에러 스펙 적재 endpoints={}", loaded.size());
        return loaded;
    }

    private ErrorSpec share(ErrorSpec spec, Map<String, String> pool) {
        return new ErrorSpec(pool.computeIfAbsent(spec.path(), k -> k),
                pool.computeIfAbsent(spec.security(), k -> k),
                spec.own().stream().map(e -> share(e, pool)).toList(),
                spec.common().stream().map(e -> share(e, pool)).toList());
    }

    private ErrorSpec.Entry share(ErrorSpec.Entry entry, Map<String, String> pool) {
        return new ErrorSpec.Entry(entry.status(),
                pool.computeIfAbsent(entry.code(), k -> k),
                pool.computeIfAbsent(entry.message(), k -> k),
                entry.reason() == null ? null : pool.computeIfAbsent(entry.reason(), k -> k),
                entry.fieldErrors(),
                entry.scope() == null ? null : pool.computeIfAbsent(entry.scope(), k -> k));
    }

    @Bean
    public OperationCustomizer errorResponseCustomizer() {
        return (operation, handlerMethod) -> {
            // 오버로드된 핸들러가 있어서 파라미터 개수까지 키에 넣는다
            String key = handlerMethod.getBeanType().getSimpleName()
                    + "#" + handlerMethod.getMethod().getName()
                    + "/" + handlerMethod.getMethod().getParameterCount();
            ErrorSpec spec = catalog.get(key);
            if (spec == null) return operation;

            // 인증 계열은 전 API 공통이라 문서 상단에 한 번만 싣는다. 여기서는 이 API 고유의 에러만 남긴다.
            List<ErrorSpec.Entry> own = spec.all().stream()
                    .filter(e -> !e.isAuth() && !e.isGlobal())
                    .toList();
            if (own.isEmpty()) return operation;

            ApiResponses responses = operation.getResponses() != null ? operation.getResponses() : new ApiResponses();
            own.stream()
                    .collect(Collectors.groupingBy(ErrorSpec.Entry::status, LinkedHashMap::new, Collectors.toList()))
                    .forEach((status, entries) ->
                            responses.addApiResponse(String.valueOf(status), response(entries, sampleFieldErrors(handlerMethod))));
            operation.setResponses(responses);
            return operation;
        };
    }

    private ApiResponse response(List<ErrorSpec.Entry> entries, List<Map<String, Object>> sampleFieldErrors) {
        Map<String, Example> examples = new LinkedHashMap<>();
        for (ErrorSpec.Entry entry : entries) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("isSuccess", false);
            body.put("code", entry.code());
            body.put("message", entry.message());
            body.put("timestamp", SAMPLE_TIMESTAMP);
            // 검증 실패 계열만 필드 단위 오류를 함께 내려준다 (다른 에러에서는 아예 빠진다)
            if (entry.fieldErrors() && !sampleFieldErrors.isEmpty()) body.put("fieldErrors", sampleFieldErrors);

            Example example = new Example().summary(entry.message()).value(body);
            if (entry.reason() != null) example.description(entry.reason());
            examples.put(entry.code(), example);
        }

        MediaType mediaType = new MediaType().schema(new Schema<>().$ref(SCHEMA_REF));
        examples.forEach(mediaType::addExamples);

        return new ApiResponse()
                .description(describe(entries))
                .content(new Content().addMediaType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, mediaType));
    }

    /**
     * 예시는 그 API의 요청 DTO에서 뽑는다. 고정값을 쓰면 어느 API를 봐도 같은 필드가 나온다.
     * 실제 응답에는 그때 실패한 필드만 담기지만, 문서에서는 어떤 필드가 걸릴 수 있는지 전부 보여준다.
     */
    private List<Map<String, Object>> sampleFieldErrors(HandlerMethod handlerMethod) {
        for (MethodParameter parameter : handlerMethod.getMethodParameters()) {
            // 멀티파트 엔드포인트는 @RequestBody 가 아니라 @RequestPart 로 본문을 받는다
            boolean body = parameter.getParameterAnnotation(RequestBody.class) != null
                    || parameter.getParameterAnnotation(RequestPart.class) != null;
            if (!body) continue;

            Class<?> type = parameter.getParameterType();
            RecordComponent[] components = type.getRecordComponents();
            if (components == null) continue;

            List<Map<String, Object>> samples = new ArrayList<>();
            for (RecordComponent component : components) {
                for (Annotation constraint : constraintsOf(type, component)) {
                    if (samples.size() >= MAX_FIELD_ERROR_SAMPLES) return samples;
                    samples.add(fieldError(component, constraint));
                }
            }
            // 클래스 레벨 제약(조건부 필수·필드 간 규칙)은 어노테이션이 대상 필드를 들고 있다
            for (Annotation constraint : type.getAnnotations()) {
                if (!constraint.annotationType().isAnnotationPresent(Constraint.class)) continue;
                String field = targetFieldOf(constraint);
                if (field == null || samples.size() >= MAX_FIELD_ERROR_SAMPLES) continue;
                samples.add(typeLevelFieldError(field, constraint));
            }
            if (!samples.isEmpty()) return samples;
        }
        return List.of();
    }

    /** 제약은 record 컴포넌트가 아니라 필드나 접근자에 붙는다 (@Target 에 RECORD_COMPONENT 가 없다). */
    private List<Annotation> constraintsOf(Class<?> type, RecordComponent component) {
        List<Annotation> candidates = new ArrayList<>(List.of(component.getAnnotations()));
        candidates.addAll(List.of(component.getAccessor().getAnnotations()));
        try {
            candidates.addAll(List.of(type.getDeclaredField(component.getName()).getAnnotations()));
        } catch (NoSuchFieldException ignored) {
            // 필드가 없으면 접근자만 본다
        }

        return candidates.stream()
                .filter(a -> a.annotationType().getName().startsWith(CONSTRAINT_PACKAGE))
                .collect(Collectors.toMap(a -> a.annotationType().getSimpleName(), a -> a, (a, b) -> a, LinkedHashMap::new))
                .values().stream().toList();
    }

    private Map<String, Object> fieldError(RecordComponent component, Annotation annotation) {
        Map<String, Object> fieldError = new LinkedHashMap<>();
        fieldError.put("field", component.getName());
        fieldError.put("rejectedValue", component.getType() == String.class ? "" : null);
        fieldError.put("reason", annotation.annotationType().getSimpleName());
        fieldError.put("message", messageOf(annotation));
        return fieldError;
    }

    /** 대상 필드를 선언하지 않은 제약은 어느 필드에 붙일지 알 수 없어 예시에서 건너뛴다. */
    private String targetFieldOf(Annotation annotation) {
        try {
            Object value = annotation.annotationType().getMethod("field").invoke(annotation);
            String field = String.valueOf(value);
            return field.isBlank() ? null : field;
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> typeLevelFieldError(String field, Annotation annotation) {
        Map<String, Object> fieldError = new LinkedHashMap<>();
        fieldError.put("field", field);
        fieldError.put("rejectedValue", null);
        fieldError.put("reason", annotation.annotationType().getSimpleName());
        fieldError.put("message", messageOf(annotation));
        return fieldError;
    }

    private String messageOf(Annotation annotation) {
        try {
            return String.valueOf(annotation.annotationType().getMethod("message").invoke(annotation));
        } catch (Exception e) {
            return "요청값이 올바르지 않습니다.";
        }
    }

    /** 인증 계열은 전 API 공통이라 개수를 세봐야 의미가 없다. 도메인 에러만 코드 묶음으로 안내한다. */
    private String describe(List<ErrorSpec.Entry> entries) {
        if (entries.size() == 1) return entries.get(0).message();
        if (entries.stream().allMatch(ErrorSpec.Entry::isAuth)) return "인증";

        String families = entries.stream()
                .filter(e -> !e.isAuth())
                .map(e -> family(e.code()))
                .distinct()
                .map(f -> f + " " + entries.stream().filter(e -> !e.isAuth() && family(e.code()).equals(f)).count() + "개")
                .collect(Collectors.joining(", "));

        return families.isEmpty() ? "인증" : families + " - code 로 구분해주세요";
    }

    private static String family(String code) {
        return code.replaceAll("_\\d+$", "");
    }

    /** 어느 API에서나 같은 모양으로 나가는 에러는 문서 상단에 한 번만 싣는다. */
    private String commonErrorTables() {
        String auth = table(
                "인증 공통 에러",
                "인증이 필요한 모든 API에서 아래 에러가 나갈 수 있습니다. 각 API 응답 목록에는 반복해서 싣지 않습니다.",
                entries(e -> e.isAuth() && (e.reason() == null || !e.reason().startsWith("hasRole("))));

        String global = table(
                "요청 공통 에러",
                "API 성격과 무관하게 나갈 수 있는 에러입니다.",
                entries(ErrorSpec.Entry::isGlobal));

        String roleNote = auth.isEmpty() ? ""
                : "\n권한이 모자라면 `COMMON_ERROR_003` (403) 이 나갑니다. 필요한 권한은 각 API의 자물쇠 표시를 확인하세요.\n";

        // 성격이 다른 표라 구분선으로 끊는다
        return Stream.of(auth + roleNote, global, websocketTable())
                .filter(section -> !section.isBlank())
                .collect(Collectors.joining("\n---\n"));
    }

    private String websocketTable() {
        if (websocket == null || websocket.frames() == null || websocket.frames().isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n## 웹소켓(STOMP) 에러\n\n");
        sb.append("REST 와 응답 형식이 다릅니다. `ErrorResponse` 가 아니라 ERROR 프레임의 message 문자열로 옵니다.\n\n");
        sb.append("| message | 발생 지점 |\n|---|---|\n");
        for (WebsocketErrorSpec.Frame f : websocket.frames()) {
            sb.append("| `").append(f.message()).append("` | ").append(f.source()).append(" |\n");
        }
        sb.append("\n토큰 만료·잘못된 토큰·권한값 이상이 모두 `UNAUTHORIZED` 하나로 묶여 나갑니다.\n");

        if (websocket.codes() != null && !websocket.codes().isEmpty()) {
            sb.append("\n메시지 발행이 실패하면 아래 코드가 서버 로그와 응답에 남습니다.\n\n");
            sb.append("| status | code | 설명 |\n|---|---|---|\n");
            for (ErrorSpec.Entry e : websocket.codes()) {
                sb.append("| ").append(e.status()).append(" | `").append(e.code()).append("` | ").append(e.message()).append(" |\n");
            }
        }
        return sb.toString();
    }

    private List<ErrorSpec.Entry> entries(java.util.function.Predicate<ErrorSpec.Entry> filter) {
        return catalog.values().stream()
                .flatMap(spec -> spec.common().stream())
                .filter(filter)
                .collect(Collectors.toMap(ErrorSpec.Entry::code, e -> e, (a, b) -> a, LinkedHashMap::new))
                .values().stream()
                .sorted(Comparator.comparingInt(ErrorSpec.Entry::status).thenComparing(ErrorSpec.Entry::code))
                .toList();
    }

    private String table(String title, String note, List<ErrorSpec.Entry> rows) {
        if (rows.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n## ").append(title).append("\n\n").append(note).append("\n\n");
        sb.append("| status | code | 설명 |\n|---|---|---|\n");
        for (ErrorSpec.Entry e : rows) {
            sb.append("| ").append(e.status()).append(" | `").append(e.code()).append("` | ").append(e.message()).append(" |\n");
        }
        return sb.toString();
    }

    /** 에러 응답을 반환하는 핸들러가 없어서 ErrorResponse 스키마가 자동 등록되지 않는다. 직접 넣어준다. */
    @Bean
    public GlobalOpenApiCustomizer errorSchemaRegistrar() {
        return openApi -> {
            if (openApi.getInfo() != null) {
                String base = openApi.getInfo().getDescription() == null ? "" : openApi.getInfo().getDescription();
                openApi.getInfo().setDescription(base + commonErrorTables());
            }
            ResolvedSchema resolved = ModelConverters.getInstance()
                    .resolveAsResolvedSchema(new AnnotatedType(ErrorResponse.class).resolveAsRef(false));
            if (resolved == null || resolved.schema == null) return;

            if (openApi.getComponents() == null) openApi.setComponents(new Components());
            Components components = openApi.getComponents();
            components.addSchemas(SCHEMA_NAME, resolved.schema);
            resolved.referencedSchemas.forEach(components::addSchemas);
        };
    }
}
