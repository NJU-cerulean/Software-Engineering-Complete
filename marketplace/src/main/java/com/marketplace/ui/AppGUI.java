package com.marketplace.ui;

import com.marketplace.dao.ProductDAO;
import com.marketplace.dao.MerchantDAO;
import com.marketplace.service.AdminService;
import com.marketplace.service.AuthService;
import com.marketplace.service.MessageService;
import com.marketplace.service.OrderService;
import com.marketplace.service.ProductService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * 一个非常简陋的 Swing GUI，用来演示基本功能：浏览/搜索商品、登录/注册、购买、私信与管理员操作。
 * 说明：此 GUI 仅用于快速交互示例，UI/异常/输入校验非常简单。
 */
public class AppGUI {
    private final AuthService auth = new AuthService();
    private final ProductService productService = new ProductService();
    private final OrderService orderService = new OrderService();
    private final MessageService messageService = new MessageService();
    private final AdminService adminService = new AdminService();
    private final ProductDAO productDAO = new ProductDAO();
    private final MerchantDAO merchantDAO = new MerchantDAO();

    private JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppGUI().createAndShow());
    }

    private void createAndShow() {
        frame = new JFrame("Marketplace GUI Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setLayout(new FlowLayout());

        JButton btnList = new JButton("浏览商品");
        JButton btnSearch = new JButton("搜索商品");
        JButton btnUser = new JButton("用户 登录/注册");
        JButton btnMerchant = new JButton("商家 登录/注册");
        JButton btnAdmin = new JButton("管理员 登录");
        JButton btnExit = new JButton("退出");

        top.add(btnList);
        top.add(btnSearch);
        top.add(btnUser);
        top.add(btnMerchant);
        top.add(btnAdmin);
        top.add(btnExit);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        JScrollPane sp = new JScrollPane(area);

        frame.add(top, BorderLayout.NORTH);
        frame.add(sp, BorderLayout.CENTER);

        btnList.addActionListener(e -> {
            try {
                List<com.marketplace.models.Product> list = productService.listPublished();
                area.setText(buildProductList(list));
            } catch (SQLException ex) {
                showError(ex);
            }
        });

        btnSearch.addActionListener(e -> {
            String kw = JOptionPane.showInputDialog(frame, "输入搜索关键词:");
            if (kw == null) return;
            try {
                List<com.marketplace.models.Product> list = productService.searchProducts(kw);
                area.setText(buildProductList(list));
            } catch (SQLException ex) {
                showError(ex);
            }
        });

        btnUser.addActionListener(e -> userFlow(area));
        btnMerchant.addActionListener(e -> merchantFlow(area));
        btnAdmin.addActionListener(e -> adminFlow(area));

        btnExit.addActionListener(e -> System.exit(0));

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private String buildProductList(List<com.marketplace.models.Product> list) {
        if (list == null || list.isEmpty()) return "无商品";
        StringBuilder sb = new StringBuilder();
        for (com.marketplace.models.Product p : list) {
            // 为了保护商家/用户隐私，不在商品列表中直接显示联系方式。用户可使用“私信商家”或购买后通过受控流程交换联系方式。
            sb.append(p.getProductId()).append(" | ").append(p.getTitle()).append(" | ").append(p.getPrice()).append(" | 库存:").append(p.getStock()).append(" | 联系方式: 使用站内私信或购买后交换\n");
        }
        return sb.toString();
    }

    private void userFlow(JTextArea area) {
        String[] opts = {"注册", "登录", "取消"};
        int sel = JOptionPane.showOptionDialog(frame, "选择操作", "用户", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
        try {
            if (sel == 0) { // register
                String name = JOptionPane.showInputDialog(frame, "用户名:");
                String phone = JOptionPane.showInputDialog(frame, "手机号:");
                String pwd = JOptionPane.showInputDialog(frame, "密码:");
                if (name == null || phone == null || pwd == null) return;
                boolean ok = auth.registerUser(new com.marketplace.models.User(name, phone, pwd), pwd);
                JOptionPane.showMessageDialog(frame, ok ? "注册成功" : "注册失败");
            } else if (sel == 1) { // login
                String phone = JOptionPane.showInputDialog(frame, "手机号:");
                String pwd = JOptionPane.showInputDialog(frame, "密码:");
                if (phone == null || pwd == null) return;
                boolean ok = auth.loginUser(phone, pwd);
                if (!ok) { JOptionPane.showMessageDialog(frame, "登录失败"); return; }
                JOptionPane.showMessageDialog(frame, "登录成功");
                // 用户菜单简化
                String[] um = {"列出商品", "搜索", "购买", "查看消息", "私信商家", "退出"};
                while (true) {
                    int u = JOptionPane.showOptionDialog(frame, "用户操作", "用户菜单", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, um, um[0]);
                    if (u == 0) { area.setText(buildProductList(productService.listPublished())); }
                    else if (u == 1) { String kw = JOptionPane.showInputDialog(frame, "关键词:"); if (kw!=null) area.setText(buildProductList(productService.searchProducts(kw))); }
                    else if (u == 2) { String pid = JOptionPane.showInputDialog(frame, "商品ID:"); String qtys = JOptionPane.showInputDialog(frame, "数量:"); if (pid!=null&&qtys!=null) { int qty=Integer.parseInt(qtys); productDAO.reduceStock(pid, qty); com.marketplace.models.Product prod = productDAO.findById(pid); orderService.createOrder(phone, prod.getMerchantId(), prod.getPrice()*qty,0,0); // 交换联系方式
                        messageService.sendMessagePublic(phone, prod.getMerchantPhone(), "用户联系方式:"+phone);
                        messageService.sendMessagePublic(prod.getMerchantPhone(), phone, "商家联系方式:"+prod.getMerchantPhone());
                        JOptionPane.showMessageDialog(frame, "购买成功"); }}
                    else if (u == 3) { List<String> msgs = messageService.getMessagesFor(phone); JOptionPane.showMessageDialog(frame, msgs.isEmpty()?"无消息":String.join("\n", msgs)); }
                    else if (u == 4) { String to = JOptionPane.showInputDialog(frame, "商家手机号:"); String content = JOptionPane.showInputDialog(frame, "内容:"); if (to!=null&&content!=null) { messageService.sendMessagePublic(phone,to,content); JOptionPane.showMessageDialog(frame, "已发送"); }}
                    else break;
                }
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void merchantFlow(JTextArea area) {
        String[] opts = {"注册", "登录", "取消"};
        int sel = JOptionPane.showOptionDialog(frame, "选择操作", "商家", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
        try {
            if (sel == 0) { // register
                String shop = JOptionPane.showInputDialog(frame, "店铺名:");
                String phone = JOptionPane.showInputDialog(frame, "手机号:");
                String contact = JOptionPane.showInputDialog(frame, "联系方式:");
                String pwd = JOptionPane.showInputDialog(frame, "密码:");
                if (shop==null||phone==null||pwd==null) return;
                com.marketplace.models.Merchant m = new com.marketplace.models.Merchant(shop, phone, contact, com.marketplace.models.Enums.IDENTITY.BOSS);
                boolean ok = auth.registerMerchant(m, pwd);
                JOptionPane.showMessageDialog(frame, ok?"商家注册成功":"商家注册失败");
            } else if (sel == 1) { // login
                String phone = JOptionPane.showInputDialog(frame, "手机号:");
                String pwd = JOptionPane.showInputDialog(frame, "密码:");
                if (phone==null||pwd==null) return;
                boolean ok = auth.loginMerchant(phone, pwd);
                if (!ok) { JOptionPane.showMessageDialog(frame, "登录失败"); return; }
                JOptionPane.showMessageDialog(frame, "登录成功");
                com.marketplace.models.Merchant m = merchantDAO.findByPhone(phone);
                String[] mm = {"发布商品","列出我的商品","查看消息","私信用户","退出"};
                while (true) {
                    int r = JOptionPane.showOptionDialog(frame, "商家操作", "商家菜单", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, mm, mm[0]);
                    if (r==0) { String title = JOptionPane.showInputDialog(frame,"标题:"); String desc = JOptionPane.showInputDialog(frame,"描述:"); String ps = JOptionPane.showInputDialog(frame,"价格:"); String ss = JOptionPane.showInputDialog(frame,"库存:"); if (title!=null&&ps!=null&&ss!=null){ double pr=Double.parseDouble(ps); int st=Integer.parseInt(ss); productService.publishProduct(title,desc,pr,st,m.getMerchantId(),m.getPhone()); JOptionPane.showMessageDialog(frame,"发布成功"); }}
                    else if (r==1) { area.setText(buildProductList(productDAO.listByMerchant(m.getMerchantId()))); }
                    else if (r==2) { List<String> msgs=messageService.getMessagesFor(m.getPhone()); JOptionPane.showMessageDialog(frame, msgs.isEmpty()?"无消息":String.join("\n",msgs)); }
                    else if (r==3) { String to = JOptionPane.showInputDialog(frame,"目标用户手机号:"); String content = JOptionPane.showInputDialog(frame,"内容:"); if (to!=null&&content!=null) { messageService.sendMessagePublic(m.getPhone(),to,content); JOptionPane.showMessageDialog(frame,"已发送"); }}
                    else break;
                }
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void adminFlow(JTextArea area) {
        String name = JOptionPane.showInputDialog(frame, "管理员用户名:");
        String pwd = JOptionPane.showInputDialog(frame, "密码:");
        if (name==null || pwd==null) return;
        try {
            boolean ok = auth.loginAdmin(name, pwd);
            if (!ok) { JOptionPane.showMessageDialog(frame, "登录失败"); return; }
            JOptionPane.showMessageDialog(frame, "管理员登录成功");
            String[] mm = {"搜索商品","查看所有用户手机号","查看被封商品","查看被封手机号","封禁商品","封禁手机号","取消封禁","强制删商品","退出"};
            while (true) {
                int r = JOptionPane.showOptionDialog(frame, "管理员操作", "管理员菜单", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, mm, mm[0]);
                if (r==0) { String kw = JOptionPane.showInputDialog(frame, "搜索关键词:"); if (kw!=null) { List<com.marketplace.models.Product> ps = productService.searchProducts(kw); area.setText(buildProductList(ps)); }}
                else if (r==1) { java.util.List<String> phones = adminService.listAllUserPhonesSorted(); JOptionPane.showMessageDialog(frame, phones.isEmpty()?"无用户":String.join("\n", phones)); }
                else if (r==2) { java.util.List<String> bp = adminService.listBannedProductsSorted(); JOptionPane.showMessageDialog(frame, bp.isEmpty()?"无被封商品":String.join("\n", bp)); }
                else if (r==3) { java.util.List<String> bphones = adminService.listBannedPhonesSorted(); JOptionPane.showMessageDialog(frame, bphones.isEmpty()?"无被封手机号":String.join("\n", bphones)); }
                else if (r==4) { String pid = JOptionPane.showInputDialog(frame, "商品ID (将封禁该商品):"); if (pid!=null) { adminService.banProduct(pid, "管理员封禁 by "+name); JOptionPane.showMessageDialog(frame, "商品已封禁"); }}
                else if (r==5) { String p = JOptionPane.showInputDialog(frame,"手机号:"); if (p!=null) { adminService.banPhone(p,"管理员操作 by "+name); JOptionPane.showMessageDialog(frame,"已封禁"); }}
                else if (r==6) { String p=JOptionPane.showInputDialog(frame,"手机号:"); if (p!=null){ adminService.unbanPhone(p); JOptionPane.showMessageDialog(frame,"已解禁"); }}
                else if (r==7) { String id=JOptionPane.showInputDialog(frame,"商品ID:"); if (id!=null){ adminService.forceDeleteProduct(id); JOptionPane.showMessageDialog(frame,"已删除"); }}
                else break;
            }
        } catch (SQLException ex) { showError(ex); }
    }

    private void showError(Exception e) { JOptionPane.showMessageDialog(frame, "错误: "+e.getMessage()); e.printStackTrace(); }
}
