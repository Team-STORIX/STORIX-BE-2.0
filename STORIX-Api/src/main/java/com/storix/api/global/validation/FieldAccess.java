package com.storix.api.global.validation;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 필드 이름으로 값을 읽는다. record 접근자를 먼저 보고 없으면 getter 규칙을 따른다. */
final class FieldAccess {

    // 클래스 구조는 런타임에 안 바뀌므로 조회 결과를 그대로 재사용한다 (없다는 결과도 포함)
    private static final Map<Key, Optional<Method>> CACHE = new ConcurrentHashMap<>();

    private FieldAccess() {}

    private record Key(Class<?> type, String name) {}

    static Object read(Object target, String name) {
        Method accessor = accessor(target.getClass(), name)
                .orElseThrow(() -> new IllegalStateException(notFound(target.getClass(), name)));
        try {
            return accessor.invoke(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(notFound(target.getClass(), name), e);
        }
    }

    /** 이름이 실제 접근자로 이어지는지 확인한다. 오타를 기동 전에 잡는 데 쓴다. */
    static Optional<Method> accessor(Class<?> type, String name) {
        return CACHE.computeIfAbsent(new Key(type, name), key -> find(key.type(), key.name()));
    }

    private static Optional<Method> find(Class<?> type, String name) {
        String capitalized = name.substring(0, 1).toUpperCase() + name.substring(1);
        for (String candidate : new String[] {name, "get" + capitalized, "is" + capitalized}) {
            try {
                Method method = type.getMethod(candidate);
                method.setAccessible(true);
                return Optional.of(method);
            } catch (NoSuchMethodException ignored) {
                // 다음 후보로
            }
        }
        return Optional.empty();
    }

    private static String notFound(Class<?> type, String name) {
        return "검증이 가리키는 필드를 찾지 못했습니다: " + type.getSimpleName() + "." + name;
    }
}
