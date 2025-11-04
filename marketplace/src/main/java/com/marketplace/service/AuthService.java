package com.marketplace.service;

import com.marketplace.dao.MerchantDAO;
import com.marketplace.dao.UserDAO;
import com.marketplace.db.DBUtil;
import com.marketplace.models.Merchant;
import com.marketplace.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 认证服务：处理用户与商家的注册与登录逻辑。
 */
public class AuthService {
    private final UserDAO userDAO = new UserDAO();
    private final MerchantDAO merchantDAO = new MerchantDAO();
    private final AdminService adminService = new AdminService();

    /**
     * 注册普通用户，返回是否成功
     */
    public boolean registerUser(User u, String password) throws SQLException {
        // 若手机号在黑名单中，则禁止注册
        if (adminService.isBanned(u.getPhone())) return false;
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT OR IGNORE INTO users (id, username, phone, password, vip, login_count) VALUES (?, ?, ?, ?, ?, ?)") ) {
            ps.setString(1, u.getUserId());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPhone());
            ps.setString(4, password);
            ps.setString(5, "NORMAL");
            ps.setInt(6, 0);
            int affected = ps.executeUpdate();
            return affected > 0;
        }
    }

    /**
     * 用户登录校验
     */
    public boolean loginUser(String phone, String password) throws SQLException {
        // 禁止被封禁的用户登录
        if (adminService.isBanned(phone)) return false;
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT password FROM users WHERE phone = ?")) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String p = rs.getString(1);
                    return p != null && p.equals(password);
                }
            }
        }
        return false;
    }

    /**
     * 注册商家并持久化密码
     */
    public boolean registerMerchant(Merchant m, String password) throws SQLException {
        // 若手机号在黑名单中，则禁止注册
        if (adminService.isBanned(m.getPhone())) return false;
        // persist merchant with password
        com.marketplace.dao.MerchantDAO dao = new com.marketplace.dao.MerchantDAO();
        dao.save(m, password);
        return true;
    }

    /**
     * 商家登录校验
     */
    public boolean loginMerchant(String phone, String password) throws SQLException {
        // 禁止被封禁的商家登录
        if (adminService.isBanned(phone)) return false;
        com.marketplace.dao.MerchantDAO dao = new com.marketplace.dao.MerchantDAO();
        com.marketplace.models.Merchant m = dao.findByPhone(phone);
        return m != null && m.login(password);
    }

    /**
     * 管理员登录校验（管理员账号由数据库预置，禁止在程序中注册）
     */
    public boolean loginAdmin(String username, String password) 
    {
        if(username.equals("admin") && password.equals("123456"))
        {
            return true;
        }
        return false;
    }
}
