package com.marketplace.service;

import com.marketplace.db.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.marketplace.dao.ProductDAO;
import com.marketplace.models.Enums;

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
     * 管理员一键清空主要数据（仅删除数据，保留表结构与管理员账号）
     */
    public void clearAllData() throws SQLException {
        DBUtil.clearAllData();
    }

    /**
     * 管理员加载样例数据（基础商家与商品）
     */
    public void seedSampleData() throws SQLException {
        DBUtil.seedSampleData();
    }

    /**
     * 列出被封禁的商品（按商品 title 的字典序）
     * 返回格式："productId | title"
     */
    public java.util.List<String> listBannedProductsSorted() throws SQLException {
        java.util.List<String> res = new java.util.ArrayList<>();
        String sql = "SELECT p.id, p.title FROM products p JOIN banned_products b ON p.id = b.product_id ORDER BY p.title COLLATE NOCASE ASC";
        try (Connection c = DBUtil.getConnection(); PreparedStatement ps = c.prepareStatement(sql); java.sql.ResultSet rs = ps.executeQuery()) {
            while (rs.next()) res.add(rs.getString("id") + " | " + rs.getString("title"));
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

    /**
     * 根据关键字搜索用户（用户名或手机号匹配），返回格式 "userId | username | phone"
     */
    public java.util.List<String> searchUsersByKeyword(String kw) throws SQLException {
        java.util.List<String> res = new java.util.ArrayList<>();
        String sql = "SELECT id, username, phone FROM users WHERE username LIKE ? OR phone LIKE ? LIMIT 100";
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String pattern = "%" + (kw == null ? "" : kw) + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    res.add(rs.getString("id") + " | " + rs.getString("username") + " | " + rs.getString("phone"));
                }
            }
        }
        return res;
    }

    /**
     * 列出当前开启状态（OPEN）的投诉（接口方法，暂不实现具体逻辑）
     * 管理员界面可调用此方法显示待处理投诉列表。
     */
    public java.util.List<String> listOpenComplaints() throws SQLException {
        throw new UnsupportedOperationException("AdminService.listOpenComplaints not implemented; use ComplaintService or implement DAO logic");
    }

    /**
     * 更新投诉状态（例如标记为 IN_PROGRESS 或 RESOLVED）。
     * 仅定义接口；实际实现可调用 ComplaintDAO/ComplaintService 进行状态更新并记录处理人/时间。
     */
    public boolean updateComplaintStatus(String complaintId, Enums.ComplaintStatus status) throws SQLException {
        throw new UnsupportedOperationException("AdminService.updateComplaintStatus not implemented");
    }
}
