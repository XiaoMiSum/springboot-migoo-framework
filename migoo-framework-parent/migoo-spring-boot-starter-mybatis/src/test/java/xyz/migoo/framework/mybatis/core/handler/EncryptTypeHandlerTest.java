package xyz.migoo.framework.mybatis.core.handler;

import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EncryptTypeHandlerTest {

    private static final String ENCRYPTOR_PASSWORD_KEY = "mybatis-plus.encryptor.password";

    private final EncryptTypeHandler handler = new EncryptTypeHandler();

    @BeforeAll
    static void setUpPassword() {
        System.setProperty(ENCRYPTOR_PASSWORD_KEY, "test-encrypt-password");
    }

    @AfterAll
    static void tearDownPassword() {
        System.clearProperty(ENCRYPTOR_PASSWORD_KEY);
    }

    @Test
    void encryptReturnsNullForNull() {
        assertThat(EncryptTypeHandler.encrypt(null)).isNull();
    }

    @Test
    void encryptProducesNonPlainText() {
        String encrypted = EncryptTypeHandler.encrypt("hello");
        assertThat(encrypted).isNotBlank();
        assertThat(encrypted).isNotEqualTo("hello");
    }

    @Test
    void setNonNullParameterEncryptsValue() throws Exception {
        PreparedStatement ps = mock(PreparedStatement.class);

        handler.setNonNullParameter(ps, 1, "hello", JdbcType.VARCHAR);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(ps).setString(org.mockito.ArgumentMatchers.eq(1), captor.capture());
        assertThat(captor.getValue()).isNotEqualTo("hello");
        assertThat(captor.getValue()).isEqualTo(EncryptTypeHandler.encrypt("hello"));
    }

    @Test
    void getNullableResultDecryptsValue() throws Exception {
        String encrypted = EncryptTypeHandler.encrypt("hello");
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("phone")).thenReturn(encrypted);

        assertThat(handler.getNullableResult(rs, "phone")).isEqualTo("hello");
    }

    @Test
    void getNullableResultReturnsNullWhenValueNull() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("phone")).thenReturn(null);

        assertThat(handler.getNullableResult(rs, "phone")).isNull();
        assertThat(handler.getNullableResult(rs, 1)).isNull();
    }
}
