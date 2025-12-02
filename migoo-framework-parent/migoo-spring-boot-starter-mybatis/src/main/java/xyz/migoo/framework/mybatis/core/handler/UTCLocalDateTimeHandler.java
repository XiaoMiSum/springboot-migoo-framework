package xyz.migoo.framework.mybatis.core.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class UTCLocalDateTimeHandler extends BaseTypeHandler<LocalDateTime> {

    private static final ZoneId UTC = ZoneId.of("UTC");

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i,
                                    LocalDateTime parameter, JdbcType jdbcType) throws SQLException {
        // LocalDateTime → Instant (UTC) → Timestamp
        Instant instant = parameter.atZone(ZoneId.systemDefault()).toInstant();
        ps.setTimestamp(i, Timestamp.from(instant));
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Timestamp ts = rs.getTimestamp(columnName);
        return toLocalDateTime(ts);
    }

    @Override
    public LocalDateTime getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Timestamp ts = rs.getTimestamp(columnIndex);
        return toLocalDateTime(ts);
    }

    @Override
    public LocalDateTime getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Timestamp ts = cs.getTimestamp(columnIndex);
        return toLocalDateTime(ts);
    }

    private LocalDateTime toLocalDateTime(Timestamp ts) {
        if (ts == null) return null;
        // Timestamp → Instant (UTC) → LocalDateTime（系统默认时区）
        Instant instant = ts.toInstant();
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    }
}
