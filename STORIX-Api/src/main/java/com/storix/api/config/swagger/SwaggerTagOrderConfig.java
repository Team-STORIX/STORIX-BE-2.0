package com.storix.api.config.swagger;

import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 태그가 컨트롤러 스캔 순서대로 나와 찾기 어렵다. 이름순으로 고정한다.
 * 문서에 선언되지 않은 태그는 경로에서 모아 함께 정렬한다.
 */
@Configuration
public class SwaggerTagOrderConfig {

    @Bean
    public GlobalOpenApiCustomizer tagOrderByName() {
        return openApi -> {
            Map<String, Tag> tags = new LinkedHashMap<>();

            if (openApi.getTags() != null) {
                openApi.getTags().forEach(tag -> tags.put(tag.getName(), tag));
            }
            if (openApi.getPaths() != null) {
                openApi.getPaths().values().stream()
                        .flatMap(pathItem -> pathItem.readOperations().stream())
                        .filter(operation -> operation.getTags() != null)
                        .flatMap(operation -> operation.getTags().stream())
                        .forEach(name -> tags.computeIfAbsent(name, key -> new Tag().name(key)));
            }
            if (tags.isEmpty()) return;

            List<Tag> sorted = tags.values().stream()
                    .sorted(Comparator.comparing(Tag::getName))
                    .toList();
            openApi.setTags(sorted);
        };
    }
}
