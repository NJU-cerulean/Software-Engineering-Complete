package com.marketplace.service;

import com.marketplace.db.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.marketplace.dao.ProductDAO;

/**
 * 管理员服务：提供封禁/解封电话和检查封禁状态的操作。
 */
public class AdminService {
    /**
     * 封禁手机号（插入或替换记录）
     */
    public boolean banPhone(String phone, String reason) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT OR REPLACE INTO banned (phone, reason) VALUES (?, ?)") ) {
            ps.setString(1, phone);
            ps.setString(2, reason);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * 取消封禁手机号
     */
    public boolean unbanPhone(String phone) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM banned WHERE phone = ?") ) {
            ps.setString(1, phone);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * 判断手机号是否已被封禁
     */
    public boolean isBanned(String phone) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT phone FROM banned WHERE phone = ?")) {
            ps.setString(1, phone);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * 管理员强制删除商品
     */
    public boolean forceDeleteProduct(String productId) throws SQLException {
        ProductDAO dao = new ProductDAO();
        dao.deleteProduct(productId);
        return true;
    }

    /**
     * 管理员封禁商品（记录到 banned_products）
     */
    public boolean banProduct(String productId, String reason) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT OR REPLACE INTO banned_products (product_id, reason) VALUES (?, ?)") ) {
            ps.setString(1, productId);
            ps.setString(2, reason);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * 解除商品封禁
     */
    public boolean unbanProduct(String productId) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM banned_products WHERE product_id = ?") ) {
            ps.setString(1, productId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * 列出被封禁的商品（按商品 title 的字典序）
     * 返回格式："productId | title"
     */
    public java.util.List<String> listBannedProductsSorted() throws SQLException {
        java.util.List<String> res = new java.util.ArrayList<>();
        String sql = "SELECT p.id, p.title FROM products p JOIN banned_products b ON p.id = b.product_id ORDER BY p.title COLLATE NOCASE ASC";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                res.add(rs.getString("id") + " | " + rs.getString("title"));
            }
        }
        return res;
    }

    /**
     * 列出所有被封禁的手机号（按数字大小排序，尽量按整数排序）
     */
    public java.util.List<String> listBannedPhonesSorted() throws SQLException {
        java.util.List<String> res = new java.util.ArrayList<>();
        String sql = "SELECT phone FROM banned ORDER BY CAST(phone AS INTEGER) ASC, phone ASC";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) res.add(rs.getString("phone"));
        }
        return res;
    }

    /**
     * 列出所有注册用户的手机号（按数字大小排序）
     */
    public java.util.List<String> listAllUserPhonesSorted() throws SQLException {
        java.util.List<String> res = new java.util.ArrayList<>();
        String sql = "SELECT phone FROM users WHERE phone IS NOT NULL ORDER BY CAST(phone AS INTEGER) ASC, phone ASC";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) res.add(rs.getString("phone"));
        }
        return res;
    }
}
