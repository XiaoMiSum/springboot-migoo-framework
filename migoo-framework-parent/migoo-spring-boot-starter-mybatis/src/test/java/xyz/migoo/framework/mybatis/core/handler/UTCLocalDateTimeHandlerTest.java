package xyz.migoo.framework.mybatis.core.handler;

import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UTCLocalDateTimeHandlerTest {

    private final UTCLocalDateTimeHandler handler = new UTCLocalDateTimeHandler();

    @Test
    void setNonNullParameterConvertsToUtcTimestamp() throws Exception {
        LocalDateTime value = LocalDateTime.of(2024, 5, 1, 10, 30, 0);
        Instant expectedInstant = value.atZone(ZoneId.systemDefault()).toInstant();
        PreparedStatement ps = mock(PreparedStatement.class);

        handler.setNonNullParameter(ps, 1, value, JdbcType.TIMESTAMP);

        verify(ps).setTimestamp(1, Timestamp.from(expectedInstant));
    }

    @Test
    void getNullableResultConvertsTimestampBackToUtc() throws Exception {
        LocalDateTime value = LocalDateTime.of(2024, 5, 1, 10, 30, 0);
        Timestamp ts = Timestamp.from(value.atZone(ZoneId.systemDefault()).toInstant());
        ResultSet rs = mock(ResultSet.class);
        when(rs.getTimestamp("create_time")).thenReturn(ts);

        LocalDateTime result = handler.getNullableResult(rs, "create_time");

        assertThat(result).isEqualTo(LocalDateTime.ofInstant(ts.toInstant(), ZoneOffset.UTC));
    }

    @Test
    void getNullableResultReturnsNullWhenTimestampNull() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getTimestamp("create_time")).thenReturn(null);

        assertThat(handler.getNullableResult(rs, "create_time")).isNull();
        assertThat(handler.getNullableResult(rs, 1)).isNull();
    }

    @Test
    void getNullableResultByColumnIndex() throws Exception {
        LocalDateTime value = LocalDateTime.of(2024, 5, 1, 10, 30, 0);
        Timestamp ts = Timestamp.from(value.atZone(ZoneId.systemDefault()).toInstant());
        ResultSet rs = mock(ResultSet.class);
        when(rs.getTimestamp(1)).thenReturn(ts);

        assertThat(handler.getNullableResult(rs, 1))
                .isEqualTo(LocalDateTime.ofInstant(ts.toInstant(), ZoneOffset.UTC));
    }
}
