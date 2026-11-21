package com.storix.domain.domains.aichat.converter;

import com.storix.domain.domains.works.domain.WorksType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

// "WEBTOON,BOOK" 처럼 enum 이름을 쉼표로 이어 저장한다. 빈 문자열은 상관없음
@Converter
public class AiChatWorksTypesConverter implements AttributeConverter<Set<WorksType>, String> {

    @Override
    public String convertToDatabaseColumn(Set<WorksType> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "";
        }
        return EnumSet.copyOf(attribute).stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }

    @Override
    public Set<WorksType> convertToEntityAttribute(String dbData) {
        Set<WorksType> result = EnumSet.noneOf(WorksType.class);
        if (dbData == null || dbData.isBlank()) {
            return result;
        }
        Arrays.stream(dbData.split(","))
                .map(String::trim)
                .map(WorksType::valueOf)
                .forEach(result::add);
        return result;
    }
}
