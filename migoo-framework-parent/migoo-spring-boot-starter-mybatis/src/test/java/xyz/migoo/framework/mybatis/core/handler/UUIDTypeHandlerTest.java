package xyz.migoo.framework.mybatis.core.handler;

import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UUIDTypeHandlerTest {

    private final UUIDTypeHandler handler = new UUIDTypeHandler();

    @Test
    void setNonNullParameterUsesStringForVarchar() throws Exception {
        UUID uuid = UUID.randomUUID();
        PreparedStatement ps = mock(PreparedStatement.class);

        handler.setNonNullParameter(ps, 1, uuid, JdbcType.VARCHAR);

        verify(ps).setString(1, uuid.toString());
    }

    @Test
    void setNonNullParameterUsesObjectForOther() throws Exception {
        UUID uuid = UUID.randomUUID();
        PreparedStatement ps = mock(PreparedStatement.class);

        handler.setNonNullParameter(ps, 1, uuid, JdbcType.OTHER);

        verify(ps).setObject(1, uuid);
    }

    @Test
    void getNullableResultParsesStringValue() throws Exception {
        UUID uuid = UUID.randomUUID();
        ResultSet rs = mock(ResultSet.class);
        when(rs.getObject("id")).thenReturn(uuid.toString());

        assertThat(handler.getNullableResult(rs, "id")).isEqualTo(uuid);
    }

    @Test
    void getNullableResultReturnsUuidAsIs() throws Exception {
        UUID uuid = UUID.randomUUID();
        ResultSet rs = mock(ResultSet.class);
        when(rs.getObject("id")).thenReturn(uuid);

        assertThat(handler.getNullableResult(rs, "id")).isSameAs(uuid);
    }

    @Test
    void getNullableResultReturnsNullWhenValueNull() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getObject("id")).thenReturn(null);

        assertThat(handler.getNullableResult(rs, "id")).isNull();
        assertThat(handler.getNullableResult(rs, 1)).isNull();
    }
}
