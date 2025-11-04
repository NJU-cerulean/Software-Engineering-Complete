package com.marketplace.service;

import com.marketplace.dao.ProductDAO;
import com.marketplace.models.Enums;
import com.marketplace.models.Product;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * 商品服务：负责商品发布与查询等业务逻辑。
 */
public class ProductService {
    private final ProductDAO dao = new ProductDAO();

    /**
     * 发布商品（生成 id 并保存）
     */
    public Product publishProduct(String title, String desc, double price, int stock, String merchantId, String merchantPhone) throws SQLException {
        String id = "prod-" + UUID.randomUUID();
        Product p = new Product(id, title, desc, price, stock, Enums.ProductStatus.PUBLISHED, merchantId, merchantPhone);
        dao.save(p);
        return p;
    }

    /**
     * 列出已发布的商品
     */
    public List<Product> listPublished() throws SQLException {
        return dao.listPublished();
    }

    /**
     * 根据关键字搜索已发布商品（简单的包含匹配，模拟语义搜索）
     */
    public List<Product> searchProducts(String keyword) throws SQLException {
        List<Product> all = dao.listPublished();
        List<Product> res = new java.util.ArrayList<>();
        if (keyword == null || keyword.isEmpty()) return all;
        String k = keyword.toLowerCase();
        for (Product p : all) {
            if ((p.getTitle() != null && p.getTitle().toLowerCase().contains(k)) ||
                    (p.getDescription() != null && p.getDescription().toLowerCase().contains(k))) {
                res.add(p);
            }
        }
        return res;
    }
}
