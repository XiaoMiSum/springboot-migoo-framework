package xyz.migoo.framework.mybatis.core.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * UUID 类型处理器
 * <p>
 * 支持 UUID 与数据库之间的转换
 * - 数据库存储为 CHAR(36) 或 VARCHAR(36) 格式
 * - 读取时自动转换为 UUID 对象
 *
 * @author xiaomi
 */
@MappedTypes(UUID.class)
@MappedJdbcTypes({JdbcType.CHAR, JdbcType.VARCHAR})
public class UUIDTypeHandler extends BaseTypeHandler<UUID> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, UUID parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.toString());
    }

    @Override
    public UUID getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value != null ? UUID.fromString(value) : null;
    }

    @Override
    public UUID getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return value != null ? UUID.fromString(value) : null;
    }

    @Override
    public UUID getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return value != null ? UUID.fromString(value) : null;
    }
}
