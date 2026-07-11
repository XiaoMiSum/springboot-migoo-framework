package xyz.migoo.framework.mybatis.core.util;

import com.baomidou.mybatisplus.annotation.DbType;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;

/**
 * JDBC 工具类
 *
 * @author xiaomi
 */
public class JdbcUtils {

    /**
     * 判断连接是否正确
     *
     * @param url      数据源连接
     * @param username 账号
     * @param password 密码
     * @return 是否正确
     */
    public static boolean isConnectionOK(String url, String username, String password) {
        try (Connection ignored = DriverManager.getConnection(url, username, password)) {
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 获得 URL 对应的 DB 类型
     *
     * @param url URL
     * @return DB 类型
     */
    public static DbType getDbType(String url) {
        // 从URL中提取数据库类型信息
        if (url.startsWith("jdbc:mysql")) {
            return DbType.MYSQL;
        } else if (url.startsWith("jdbc:postgresql")) {
            return DbType.POSTGRE_SQL;
        } else if (url.startsWith("jdbc:oracle")) {
            return DbType.ORACLE;
        } else if (url.startsWith("jdbc:sqlserver") || url.startsWith("jdbc:microsoft")) {
            return DbType.SQL_SERVER;
        } else if (url.startsWith("jdbc:sqlite")) {
            return DbType.SQLITE;
        } else if (url.startsWith("jdbc:h2")) {
            return DbType.H2;
        } else if (url.startsWith("jdbc:dm")) {
            return DbType.DM;
        } else if (url.startsWith("jdbc:kingbase")) {
            return DbType.KINGBASE_ES;
        } else if (url.startsWith("jdbc:db2")) {
            return DbType.DB2;
        } else if (url.startsWith("jdbc:hsqldb")) {
            return DbType.HSQL;
        } else if (url.startsWith("jdbc:derby")) {
            return DbType.DERBY;
        } else if (url.startsWith("jdbc:clickhouse")) {
            return DbType.CLICK_HOUSE;
        } else if (url.startsWith("jdbc:presto")) {
            return DbType.PRESTO;
        } else if (url.startsWith("jdbc:trino")) {
            return DbType.TRINO;
        } else if (url.startsWith("jdbc:duckdb")) {
            return DbType.DUCKDB;
        }
        
        // 如果无法从URL判断，返回UNKNOWN
        return DbType.OTHER;
    }

}
