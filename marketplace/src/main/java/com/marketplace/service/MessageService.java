package com.marketplace.service;

import com.marketplace.db.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

/**
 * 消息服务：将通知写入 messages 表，模拟推送功能。
 */
public class MessageService {

    /**
     * 发送通知给商家（将消息写入 messages 表）
     */
    public void sendToMerchant(String merchantId, String content) throws SQLException {
        sendMessage("system", merchantId, content);
    }

    /**
     * 发送通知给用户（将消息写入 messages 表）
     */
    public void notifyUser(String userId, String content) throws SQLException {
        sendMessage("system", userId, content);
    }

    /**
     * 发送任意双方消息（用户 <-> 商家）并持久化
     */
    public void sendMessagePublic(String senderId, String receiverId, String content) throws SQLException {
        sendMessage(senderId, receiverId, content);
    }

    /**
     * 查询接收者的消息列表（按时间）
     */
    public java.util.List<String> getMessagesFor(String receiverId) throws SQLException {
        java.util.List<String> res = new java.util.ArrayList<>();
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT sender_id, content, timestamp FROM messages WHERE receiver_id = ? ORDER BY timestamp DESC")) {
            ps.setString(1, receiverId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String content = rs.getString(2);
                    // 在展示消息前做敏感信息掩码（例如手机号、身份证等简单数字掩码）
                    content = maskSensitiveNumbers(content);
                    res.add("from:" + rs.getString(1) + " - " + content);
                }
            }
        }
        return res;
    }

    /**
     * 以受控方式发送购买后联系方式交换（只发送掩码形式）
     */
    public void sendContactExchange(String userPhone, String merchantPhone) throws SQLException {
        String userMasked = maskPhone(userPhone);
        String merchantMasked = maskPhone(merchantPhone);
        sendMessage("system", merchantPhone, "有新订单，买家联系方式(掩码): " + userMasked);
        sendMessage("system", userPhone, "订单已创建，商家联系方式(掩码): " + merchantMasked);
    }

    // 简单掩码：保留前三位与末尾两位，其余用星号替代；若长度太短则整体用星号
    private String maskPhone(String phone) {
        if (phone == null) return "";
        String digits = phone.replaceAll("\\D", "");
        if (digits.length() <= 4) return "****";
        if (digits.length() <= 6) return digits.substring(0, 1) + "****" + digits.substring(digits.length()-1);
        String start = digits.substring(0, Math.min(3, digits.length()));
        String end = digits.substring(Math.max(digits.length()-2, 0));
        return start + "****" + end;
    }

    // 掩码任意较长数字串（用于消息内容的安全展示）
    private String maskSensitiveNumbers(String text) {
        if (text == null) return null;
        // 将连续 4 位以上的数字视为敏感信息并掩码
        return text.replaceAll("(\\d{3})\\d+(\\d{2})", "$1****$2");
    }

    private void sendMessage(String senderId, String receiverId, String content) throws SQLException {
        try (Connection c = DBUtil.getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT OR REPLACE INTO messages (id, sender_id, receiver_id, content, timestamp) VALUES (?, ?, ?, ?, ?)") ) {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, senderId);
            ps.setString(3, receiverId);
            ps.setString(4, content);
            ps.setString(5, String.valueOf(Instant.now().toEpochMilli()));
            ps.executeUpdate();
        }
    }
}
