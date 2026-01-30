package com.danceclub.club_system;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 資料庫連線測試
 * 用於驗證是否能成功連接到 Supabase PostgreSQL 資料庫
 */
@SpringBootTest
public class DatabaseConnectionTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testDatabaseConnection() throws Exception {
        System.out.println("=== 開始測試資料庫連線 ===");
        
        // 測試 DataSource 是否存在
        assertNotNull(dataSource, "DataSource 不應該為 null");
        System.out.println("✓ DataSource 已注入");

        // 測試是否能獲取連線
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "Connection 不應該為 null");
            assertTrue(connection.isValid(5), "連線應該是有效的");
            System.out.println("✓ 成功獲取資料庫連線");

            // 獲取資料庫資訊
            DatabaseMetaData metaData = connection.getMetaData();
            System.out.println("\n=== 資料庫資訊 ===");
            System.out.println("資料庫產品: " + metaData.getDatabaseProductName());
            System.out.println("資料庫版本: " + metaData.getDatabaseProductVersion());
            System.out.println("驅動程式: " + metaData.getDriverName());
            System.out.println("驅動版本: " + metaData.getDriverVersion());
            System.out.println("連線 URL: " + metaData.getURL());
            System.out.println("使用者名稱: " + metaData.getUserName());
        }

        System.out.println("\n=== 測試資料庫查詢 ===");
        
        // 測試簡單查詢
        Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
        assertEquals(1, result, "查詢結果應該是 1");
        System.out.println("✓ 簡單查詢測試成功");

        // 測試查詢當前時間
        String currentTime = jdbcTemplate.queryForObject("SELECT NOW()::TEXT", String.class);
        assertNotNull(currentTime, "當前時間不應該為 null");
        System.out.println("✓ 當前資料庫時間: " + currentTime);

        System.out.println("\n=== 資料庫連線測試完成 ===");
    }

    @Test
    public void testUserTableExists() {
        System.out.println("\n=== 測試 User 資料表是否存在 ===");
        
        String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                     "WHERE table_schema = 'public' AND table_name = 'user'";
        
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertNotNull(count, "查詢結果不應該為 null");
        
        if (count > 0) {
            System.out.println("✓ User 資料表存在");
            
            // 查詢資料表欄位
            String columnSql = "SELECT column_name, data_type " +
                             "FROM information_schema.columns " +
                             "WHERE table_schema = 'public' AND table_name = 'user' " +
                             "ORDER BY ordinal_position";
            
            System.out.println("\nUser 資料表欄位:");
            jdbcTemplate.query(columnSql, (rs) -> {
                System.out.println("  - " + rs.getString("column_name") + 
                                 " (" + rs.getString("data_type") + ")");
            });
        } else {
            System.out.println("✗ User 資料表不存在");
        }
    }

    @Test
    public void testPaymentTableExists() {
        System.out.println("\n=== 測試 Payment 資料表是否存在 ===");
        
        String sql = "SELECT COUNT(*) FROM information_schema.tables " +
                     "WHERE table_schema = 'public' AND table_name = 'payment'";
        
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        assertNotNull(count, "查詢結果不應該為 null");
        
        if (count > 0) {
            System.out.println("✓ Payment 資料表存在");
            
            // 查詢資料表欄位
            String columnSql = "SELECT column_name, data_type " +
                             "FROM information_schema.columns " +
                             "WHERE table_schema = 'public' AND table_name = 'payment' " +
                             "ORDER BY ordinal_position";
            
            System.out.println("\nPayment 資料表欄位:");
            jdbcTemplate.query(columnSql, (rs) -> {
                System.out.println("  - " + rs.getString("column_name") + 
                                 " (" + rs.getString("data_type") + ")");
            });
        } else {
            System.out.println("✗ Payment 資料表不存在");
        }
    }

    @Test
    public void testAllTables() {
        System.out.println("\n=== 列出所有資料表 ===");
        
        String sql = "SELECT table_name FROM information_schema.tables " +
                     "WHERE table_schema = 'public' " +
                     "ORDER BY table_name";
        
        jdbcTemplate.query(sql, (rs) -> {
            System.out.println("  - " + rs.getString("table_name"));
        });
    }
}
