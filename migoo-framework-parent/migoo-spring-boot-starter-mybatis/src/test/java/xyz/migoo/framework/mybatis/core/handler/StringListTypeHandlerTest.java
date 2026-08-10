package xyz.migoo.framework.mybatis.core.handler;

import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StringListTypeHandlerTest {

    private final StringListTypeHandler handler = new StringListTypeHandler();

    @Test
    void setParameterJoinsWithComma() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);

        handler.setParameter(ps, 1, Arrays.asList("a", "b", "c"), JdbcType.VARCHAR);

        verify(ps).setString(1, "a,b,c");
    }

    @Test
    void setParameterWritesEmptyForNullAndEmptyList() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);

        handler.setParameter(ps, 1, null, JdbcType.VARCHAR);
        handler.setParameter(ps, 2, Collections.emptyList(), JdbcType.VARCHAR);

        verify(ps).setString(1, "");
        verify(ps).setString(2, "");
    }

    @Test
    void getResultParsesCommaSeparatedValue() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("tags")).thenReturn("a,b,c");

        assertThat(handler.getResult(rs, "tags")).containsExactly("a", "b", "c");
    }

    @Test
    void getResultTrimsAndFiltersEmptyItems() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("tags")).thenReturn(" a ,, b ");

        assertThat(handler.getResult(rs, "tags")).containsExactly("a", "b");
    }

    @Test
    void getResultReturnsNullForNullValue() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("tags")).thenReturn(null);

        assertThat(handler.getResult(rs, "tags")).isNull();
        assertThat(handler.getResult(rs, 1)).isNull();
    }

    @Test
    void getResultReturnsNullForEmptyValue() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("tags")).thenReturn("");

        assertThat(handler.getResult(rs, "tags")).isNull();
    }
}
