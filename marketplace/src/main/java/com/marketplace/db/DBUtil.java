package com.marketplace.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库工具类：管理 SQLite 连接与初始化数据库表。
 */
public class DBUtil {
    private static final String DB_URL = "jdbc:sqlite:marketplace.db";

    static {
        try {
            initDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void initDatabase() throws SQLException {
        try (Connection conn = getConnection(); Statement st = conn.createStatement()) {
            // users
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (id TEXT PRIMARY KEY, username TEXT, phone TEXT UNIQUE, password TEXT, vip TEXT, login_count INTEGER, last_login TEXT)");
            // merchants (增加 password 字段用于认证)
            st.executeUpdate("CREATE TABLE IF NOT EXISTS merchants (id TEXT PRIMARY KEY, shop_name TEXT, phone TEXT UNIQUE, password TEXT, contact_info TEXT, employee_count INTEGER, identity TEXT)");
            // admins
            st.executeUpdate("CREATE TABLE IF NOT EXISTS admins (id TEXT PRIMARY KEY, username TEXT, password TEXT)");
            // products
            st.executeUpdate("CREATE TABLE IF NOT EXISTS products (id TEXT PRIMARY KEY, title TEXT, description TEXT, price REAL, stock INTEGER, status TEXT, merchant_id TEXT, merchant_phone TEXT)");
            // orders
            st.executeUpdate("CREATE TABLE IF NOT EXISTS orders (id TEXT PRIMARY KEY, user_id TEXT, merchant_id TEXT, total_amount REAL, discount REAL, pay_by_platform REAL, status TEXT, create_time TEXT)");
            // messages
            st.executeUpdate("CREATE TABLE IF NOT EXISTS messages (id TEXT PRIMARY KEY, sender_id TEXT, receiver_id TEXT, content TEXT, timestamp TEXT)");
            // complaints
            st.executeUpdate("CREATE TABLE IF NOT EXISTS complaints (id TEXT PRIMARY KEY, user_id TEXT, target_id TEXT, type TEXT, status TEXT)");
            // promotions
            st.executeUpdate("CREATE TABLE IF NOT EXISTS promotions (id TEXT PRIMARY KEY, merchant_id TEXT, type TEXT, discount REAL, valid_until TEXT)");
            // banned phones
            st.executeUpdate("CREATE TABLE IF NOT EXISTS banned (phone TEXT PRIMARY KEY, reason TEXT)");
            // banned products (管理员封禁的商品记录)
            st.executeUpdate("CREATE TABLE IF NOT EXISTS banned_products (product_id TEXT PRIMARY KEY, reason TEXT)");

            // seed admin (id=admin, password=123456)，管理员无法通过程序注册
            st.executeUpdate("INSERT OR IGNORE INTO admins (id, username, password) VALUES ('admin','admin','123456')");
        }
    }
}
