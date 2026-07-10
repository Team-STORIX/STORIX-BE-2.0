package com.storix.api.migration;

import com.storix.domain.domains.user.converter.OAuthRefreshTokenConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 기존 평문 oauth refresh token 일회성 암호화 마이그레이션.
 *
 * migration.oauth-token-encrypt.enabled=true 일 때만 실행.
 * 실행 절차: 프로퍼티 true로 1회 기동 → 로그로 완료 확인 → 프로퍼티 제거(또는 false).
 *
 * 컨버터를 안 타는 네이티브 SQL로 raw 평문을 읽어 Java에서 암호화 후 네이티브 UPDATE.
 * (엔티티 로드→save 방식은 더티체크에 안 걸려 재암호화가 안 되므로 사용 불가)
 * enc: 접두사/NULL은 대상에서 제외되어 재실행해도 안전(멱등).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "migration.oauth-token-encrypt.enabled", havingValue = "true")
public class OAuthRefreshTokenEncryptMigrationRunner implements ApplicationRunner {

    private static final int BATCH_SIZE = 500;

    private final JdbcTemplate jdbcTemplate;
    private final OAuthRefreshTokenConverter converter;

    @Override
    public void run(ApplicationArguments args) {
        log.info(">>> [Migration] oauth refresh token 암호화 시작");

        long lastId = 0;
        int total = 0;
        while (true) {
            // 아직 평문(enc: 아님)이면서 null 아닌 행을 user_id 커서로 배치 조회
            List<Row> rows = jdbcTemplate.query("""
                    SELECT user_id, oauth_refresh_token FROM users
                    WHERE user_id > ?
                      AND oauth_refresh_token IS NOT NULL
                      AND oauth_refresh_token NOT LIKE 'enc:%'
                    ORDER BY user_id ASC
                    LIMIT ?
                    """,
                    (rs, i) -> new Row(rs.getLong("user_id"), rs.getString("oauth_refresh_token")),
                    lastId, BATCH_SIZE);
            if (rows.isEmpty()) break;

            for (Row row : rows) {
                String encrypted = converter.convertToDatabaseColumn(row.token());
                jdbcTemplate.update(
                        "UPDATE users SET oauth_refresh_token = ? WHERE user_id = ?",
                        encrypted, row.id());
                lastId = row.id();
                total++;
            }
            log.info(">>> [Migration] 진행 {}건 (lastId={})", total, lastId);
        }

        log.info(">>> [Migration] oauth refresh token 암호화 완료 - 총 {}건", total);
    }

    private record Row(long id, String token) {
    }
}
