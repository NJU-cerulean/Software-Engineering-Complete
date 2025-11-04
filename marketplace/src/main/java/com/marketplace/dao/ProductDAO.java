package com.marketplace.dao;

import com.marketplace.db.DBUtil;
import com.marketplace.models.Enums;
import com.marketplace.models.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品数据访问对象：负责 products 表的数据读写。
 */
public class ProductDAO {
    /**
     * 保存或更新商品记录
     */
    public void save(Product p) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT OR REPLACE INTO products (id, title, description, price, stock, status, merchant_id, merchant_phone) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, p.getProductId());
            ps.setString(2, p.getTitle());
            ps.setString(3, p.getDescription());
            ps.setDouble(4, p.getPrice());
            ps.setInt(5, p.getStock());
            ps.setString(6, p.getStatus().name());
            ps.setString(7, p.getMerchantId());
            ps.setString(8, p.getMerchantPhone());
            ps.executeUpdate();
        }
    }

    /**
     * 查询已发布的商品列表
     */
    public List<Product> listPublished() throws SQLException {
        List<Product> res = new ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, title, description, price, stock, status, merchant_id, merchant_phone FROM products WHERE status = 'PUBLISHED'")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product(rs.getString("id"), rs.getString("title"), rs.getString("description"), rs.getDouble("price"), rs.getInt("stock"), Enums.ProductStatus.PUBLISHED, rs.getString("merchant_id"), rs.getString("merchant_phone"));
                    res.add(p);
                }
            }
        }
        return res;
    }

    /**
     * 减少商品库存（库存充足时）
     */
    public void reduceStock(String productId, int qty) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE products SET stock = stock - ? WHERE id = ? AND stock >= ?")) {
            ps.setInt(1, qty);
            ps.setString(2, productId);
            ps.setInt(3, qty);
            ps.executeUpdate();
        }
    }

    /**
     * 删除商品（管理员强制删除）
     */
    public void deleteProduct(String productId) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM products WHERE id = ?")) {
            ps.setString(1, productId);
            ps.executeUpdate();
        }
    }

    /**
     * 根据 id 查询商品
     */
    public Product findById(String id) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, title, description, price, stock, status, merchant_id, merchant_phone FROM products WHERE id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Enums.ProductStatus status = Enums.ProductStatus.valueOf(rs.getString("status"));
                    return new Product(rs.getString("id"), rs.getString("title"), rs.getString("description"), rs.getDouble("price"), rs.getInt("stock"), status, rs.getString("merchant_id"), rs.getString("merchant_phone"));
                }
            }
        }
        return null;
    }

    /**
     * 列出某商家的所有商品
     */
    public java.util.List<Product> listByMerchant(String merchantId) throws SQLException {
        java.util.List<Product> res = new java.util.ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT id, title, description, price, stock, status, merchant_id, merchant_phone FROM products WHERE merchant_id = ?")) {
            ps.setString(1, merchantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Enums.ProductStatus status = Enums.ProductStatus.valueOf(rs.getString("status"));
                    res.add(new Product(rs.getString("id"), rs.getString("title"), rs.getString("description"), rs.getDouble("price"), rs.getInt("stock"), status, rs.getString("merchant_id"), rs.getString("merchant_phone")));
                }
            }
        }
        return res;
    }
}
