package com.marketplace.dao;

import com.marketplace.db.DBUtil;
import com.marketplace.models.Enums;
import com.marketplace.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 用户数据访问对象：负责 users 表的读写操作。
 */
public class UserDAO {
    /**
     * 保存用户记录（若已存在则忽略）
     */
    public void save(User u) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT OR IGNORE INTO users (id, username, phone, password, vip, login_count, last_login) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, u.getUserId());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPhone());
            ps.setString(4, ""); // password persisted by auth service
            ps.setString(5, Enums.VIPLevel.NORMAL.name());
            ps.setInt(6, u.getLoginCount());
            ps.setString(7, null);
            ps.executeUpdate();
        }
    }

    /**
     * 根据手机号查找用户
     */
    public User findByPhone(String phone) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, username, phone, password FROM users WHERE phone = ?")) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User u = new User(rs.getString("username"), rs.getString("phone"), rs.getString("password"));
                    return u;
                }
            }
        }
        return null;
    }
}
