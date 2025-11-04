package com.marketplace.service;

import com.marketplace.dao.OrderDAO;
import com.marketplace.dao.ProductDAO;
import com.marketplace.models.Order;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * 订单服务：负责创建订单与按用户查询订单等功能。
 */
public class OrderService {
    private final OrderDAO orderDAO = new OrderDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final MessageService messageService = new MessageService();
    private final StatisticsService statisticsService = new StatisticsService();

    /**
     * 创建新订单并保存
     */
    public Order createOrder(String userId, String merchantId, double totalAmount, double discount, double payByPlatform) throws SQLException {
        String id = "order-" + UUID.randomUUID();
        Order o = new Order(id, userId, merchantId, totalAmount, discount, payByPlatform);
        orderDAO.save(o);
        // 下单后发送通知并记录统计
        messageService.sendToMerchant(merchantId, "新订单: " + id);
        messageService.notifyUser(userId, "订单已创建: " + id);
        statisticsService.recordOrder(o);
        return o;
    }

    /**
     * 根据用户 ID 列出订单
     */
    public List<Order> listByUser(String userId) throws SQLException {
        return orderDAO.findByUser(userId);
    }
}
