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

class LongListTypeHandlerTest {

    private final LongListTypeHandler handler = new LongListTypeHandler();

    @Test
    void setParameterJoinsWithComma() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);

        handler.setParameter(ps, 1, Arrays.asList(1L, 2L, 3L), JdbcType.VARCHAR);

        verify(ps).setString(1, "1,2,3");
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
        when(rs.getString("ids")).thenReturn("1,2,3");

        assertThat(handler.getResult(rs, "ids")).containsExactly(1L, 2L, 3L);
    }

    @Test
    void getResultReturnsNullForNullValue() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("ids")).thenReturn(null);

        assertThat(handler.getResult(rs, "ids")).isNull();
        assertThat(handler.getResult(rs, 1)).isNull();
    }

    @Test
    void getResultReturnsEmptyListForBlankValue() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("ids")).thenReturn("");

        assertThat(handler.getResult(rs, "ids")).isEmpty();
    }
}
