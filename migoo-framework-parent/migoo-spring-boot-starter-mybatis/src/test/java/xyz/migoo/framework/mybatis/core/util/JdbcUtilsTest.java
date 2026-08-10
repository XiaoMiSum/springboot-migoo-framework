package xyz.migoo.framework.mybatis.core.util;

import com.baomidou.mybatisplus.annotation.DbType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcUtilsTest {

    @Test
    void getDbType() {
        assertThat(JdbcUtils.getDbType("jdbc:mysql://localhost:3306/demo")).isEqualTo(DbType.MYSQL);
        assertThat(JdbcUtils.getDbType("jdbc:postgresql://localhost:5432/demo")).isEqualTo(DbType.POSTGRE_SQL);
        assertThat(JdbcUtils.getDbType("jdbc:oracle:thin:@localhost:1521:xe")).isEqualTo(DbType.ORACLE);
        assertThat(JdbcUtils.getDbType("jdbc:sqlserver://localhost:1433;databaseName=demo")).isEqualTo(DbType.SQL_SERVER);
        assertThat(JdbcUtils.getDbType("jdbc:microsoft:sqlserver://localhost:1433")).isEqualTo(DbType.SQL_SERVER);
        assertThat(JdbcUtils.getDbType("jdbc:sqlite:demo.db")).isEqualTo(DbType.SQLITE);
        assertThat(JdbcUtils.getDbType("jdbc:h2:mem:demo")).isEqualTo(DbType.H2);
        assertThat(JdbcUtils.getDbType("jdbc:dm://localhost:5236")).isEqualTo(DbType.DM);
        assertThat(JdbcUtils.getDbType("jdbc:kingbase://localhost:54321/demo")).isEqualTo(DbType.KINGBASE_ES);
        assertThat(JdbcUtils.getDbType("jdbc:db2://localhost:50000/demo")).isEqualTo(DbType.DB2);
        assertThat(JdbcUtils.getDbType("jdbc:clickhouse://localhost:8123/demo")).isEqualTo(DbType.CLICK_HOUSE);
        assertThat(JdbcUtils.getDbType("jdbc:trino://localhost:8080/demo")).isEqualTo(DbType.TRINO);
        assertThat(JdbcUtils.getDbType("jdbc:unknown://localhost:1234/demo")).isEqualTo(DbType.OTHER);
    }

    @Test
    void isConnectionOKReturnsFalseForUnreachableDb() {
        // 无本地数据库，连接必定失败，验证返回 false 而非抛出异常
        assertThat(JdbcUtils.isConnectionOK("jdbc:mysql://127.0.0.1:1/demo", "root", "root")).isFalse();
        assertThat(JdbcUtils.isConnectionOK("jdbc:unknown://nowhere", "", "")).isFalse();
    }
}
