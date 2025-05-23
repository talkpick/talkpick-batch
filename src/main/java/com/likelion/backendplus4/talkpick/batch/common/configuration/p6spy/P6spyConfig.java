package com.likelion.backendplus4.talkpick.batch.common.configuration.p6spy;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.P6SpyOptions;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import jakarta.annotation.PostConstruct;
import java.util.Locale;
import org.hibernate.engine.jdbc.internal.FormatStyle;
import org.springframework.context.annotation.Configuration;

/**
 * P6Spy SQL 로깅 설정 클래스
 * P6Spy 의 MessageFormattingStrategy 를 구현하여,
 * SQL 로그를 카테고리, 실행 시간, 포맷된 쿼리로 출력하도록 커스터마이징합니다.
 *
 * @since 2025-05-09
 * @modified 2025-05-09
 */
@Configuration
public class P6spyConfig implements MessageFormattingStrategy {

    /**
     * Spring 컨텍스트 초기화 직후 호출되어, P6Spy 옵션에 이 클래스의 포맷터를 사용하도록 등록합니다.
     *
     * @author 박찬병
     * @modified 2025-05-09
     * @since 2025-05-09
     */
    @PostConstruct
    public void setLogMessageFormat() {
        // P6Spy 의 ActiveInstance 에 포맷터 클래스 이름을 지정
        P6SpyOptions.getActiveInstance()
                .setLogMessageFormat(this.getClass().getName());
    }

    /**
     * 실제 로그 메시지를 생성하는 엔트리 포인트 메서드. 카테고리에 따라 SQL 을 포맷팅하고, 실행 시간과 함께 출력합니다.
     *
     * @param connectionId 커넥션 고유 ID
     * @param now          로그 출력 시각 (문자열)
     * @param elapsed      쿼리 실행 경과 시간 (ms)
     * @param category     P6Spy 로깅 카테고리 (STATEMENT, RESULT, COMMIT 등)
     * @param prepared     PreparedStatement 템플릿 (파라미터 바인딩 전 SQL)
     * @param sql          바인딩된 실제 SQL
     * @param url          데이터소스 URL
     * @return 카테고리, 실행 시간, 포맷된 SQL 을 포함한 로그 문자열
     * @author 박찬병
     * @modified 2025-05-09
     * @since 2025-05-09
     */
    @Override
    public String formatMessage(
            int connectionId,
            String now,
            long elapsed,
            String category,
            String prepared,
            String sql,
            String url
    ) {
        sql = formatSql(category, sql);
        return String.format("[%s] | %d ms | %s", category, elapsed, sql);
    }

    /**
     * SQL 문을 읽기 좋게 포맷팅합니다. DDL 문(create/alter/comment)인 경우에는 FormatStyle.DDL, 그 외 쿼리는
     * FormatStyle.BASIC 스타일을 적용합니다.
     *
     * @param category P6Spy 로깅 카테고리
     * @param sql      실제 실행된 SQL
     * @return 포맷팅된 SQL (또는 SQL 이 비어있으면 원본 반환)
     * @author 박찬병
     * @modified 2025-05-09
     * @since 2025-05-10
     */
    private String formatSql(String category, String sql) {
        if (isEmptySql(sql)) {
            return sql;
        }

        if (isStatementCategory(category)) {
            return formatStatementSql(sql);
        }

        return sql;
    }


    /**
     * SQL이 비어있는지 확인합니다.
     *
     * @param sql 실행된 SQL 문자열
     * @return 비어있으면 true, 아니면 false
     * @author 박찬병
     * @modified 2025-05-10
     * @since 2025-05-10
     */
    private boolean isEmptySql(String sql) {
        return sql == null || sql.isBlank();
    }

    /**
     * 주어진 카테고리가 STATEMENT 인지 여부를 판단합니다.
     *
     * @param category P6Spy 로깅 카테고리
     * @return STATEMENT 카테고리이면 true, 아니면 false
     * @author 박찬병
     * @modified 2025-05-10
     * @since 2025-05-10
     */
    private boolean isStatementCategory(String category) {
        return Category.STATEMENT.getName().equals(category);
    }

    /**
     * STATEMENT 카테고리의 SQL을 포맷팅합니다.
     *
     * @param sql 실행된 SQL 문자열
     * @return 포맷팅된 SQL
     * @author 박찬병
     * @modified 2025-05-09
     * @since 2025-05-10
     */
    private String formatStatementSql(String sql) {
        if (isDdlStatement(sql)) {
            return FormatStyle.DDL.getFormatter().format(sql);
        } else {
            return FormatStyle.BASIC.getFormatter().format(sql);
        }
    }

    /**
     * 주어진 SQL 문이 DDL(create/alter/comment) 문인지 여부를 판단합니다.
     *
     * @param sql 실행된 SQL 문자열
     * @return DDL 문이면 true, 아니면 false
     * @author 박찬병
     * @modified 2025-05-09
     * @since 2025-05-10
     */
    private boolean isDdlStatement(String sql) {
        String trimmedSQL = sql.trim().toLowerCase(Locale.ROOT);
        return trimmedSQL.startsWith("create")
                || trimmedSQL.startsWith("alter")
                || trimmedSQL.startsWith("comment");
    }
}