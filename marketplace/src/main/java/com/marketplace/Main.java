package com.marketplace;

import com.marketplace.models.Merchant;
import com.marketplace.models.User;
import com.marketplace.models.Product;
import com.marketplace.service.AdminService;
import com.marketplace.service.AuthService;
import com.marketplace.service.OrderService;
import com.marketplace.service.ProductService;
import com.marketplace.service.MessageService;
import com.marketplace.service.ComplaintService;
import com.marketplace.models.Enums;
import com.marketplace.dao.ProductDAO;
import com.marketplace.dao.MerchantDAO;
import com.marketplace.dao.UserDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * 主程序入口：提供控制台交互界面以演示各项功能。
 * 
 */
public class Main {
    private static final AuthService auth = new AuthService();
    private static final ProductService productService = new ProductService();
    private static final OrderService orderService = new OrderService();
    private static final AdminService adminService = new AdminService();
    private static final MessageService messageService = new MessageService();
    private static final ComplaintService complaintService = new ComplaintService();
    private static final com.marketplace.service.CartService cartService = new com.marketplace.service.CartService();
    private static final ProductDAO productDAO = new ProductDAO();
    private static final MerchantDAO merchantDAO = new MerchantDAO();
    private static final UserDAO userDAO = new UserDAO();

    private static String currentUserPhone = null; 
    private static Merchant currentMerchant = null; 
    private static String adminUser = null;

    public static void main(String[] args) {
        System.out.println("欢迎使用简易 Marketplace 系统-demo");
        try (Scanner sc = new Scanner(System.in)) {
            while (true) {
                System.out.println("主菜单：1 注册用户 2 用户登录 3 注册商家 4 商家登录 5 管理员登录 0 退出");
                String cmd = sc.nextLine().trim();
                try {
                    switch (cmd) {
                        case "1":
                            registerUser(sc);
                            break;
                        case "2":
                            userLogin(sc);
                            break;
                        case "3":
                            registerMerchant(sc);
                            break;
                        case "4":
                            merchantLogin(sc);
                            break;
                        case "5":
                            adminLogin(sc);
                            break;
                        case "0":
                            System.out.println("退出");
                            return;
                        default:
                            System.out.println("未知命令");
                    }
                } catch (SQLException e) {
                    System.out.println("操作失败：" + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    // ---------- 注册/登录/流程实现 ----------
    private static void registerUser(Scanner sc) throws SQLException {
        System.out.print("用户名: ");
        String name = sc.nextLine();
        System.out.print("手机号: ");
        String phone = sc.nextLine();
        System.out.print("密码: ");
        String pwd = sc.nextLine();
        User u = new User(name, phone, pwd);
        boolean ok = auth.registerUser(u, pwd);
        if (ok) {
            System.out.println("注册成功，已自动登录");
            currentUserPhone = phone; // 注册后默认登录
            // 登录后显示用户信息与VIP等级
            try {
                java.util.Map<String, Object> info = userDAO.getVipAndTotalByPhone(currentUserPhone);
                if (info != null) System.out.println("当前VIP: " + info.get("vip") + "，总消费: " + info.get("total_spent"));
            } catch (Exception ignored) {}
            userMenu(sc);
        } else {
            System.out.println("注册失败（可能被封禁或手机号已存在）");
        }
    }

    private static void userLogin(Scanner sc) throws SQLException {
        System.out.print("手机号: ");
        String phone = sc.nextLine();
        System.out.print("密码: ");
        String pwd = sc.nextLine();
        boolean ok = auth.loginUser(phone, pwd);
        if (ok) {
            currentUserPhone = phone;
            System.out.println("用户登录成功，进入用户菜单");
            userMenu(sc);
        } else {
            System.out.println("登录失败（检查手机号/密码或是否被封禁）");
        }
    }

    private static void registerMerchant(Scanner sc) throws SQLException {
        System.out.print("店铺名: ");
        String shop = sc.nextLine();
        System.out.print("手机号: ");
        String phone = sc.nextLine();
        System.out.print("联系方式: ");
        String contact = sc.nextLine();
        System.out.print("密码: ");
        String pwd = sc.nextLine();
        Merchant m = new Merchant(shop, phone, contact, com.marketplace.models.Enums.IDENTITY.BOSS);
        boolean ok = auth.registerMerchant(m, pwd);
        System.out.println(ok ? "商家注册成功" : "商家注册失败（可能被封禁）");
    }

    private static void merchantLogin(Scanner sc) throws SQLException {
        System.out.print("商家手机号: ");
        String phone = sc.nextLine();
        System.out.print("密码: ");
        String pwd = sc.nextLine();
        boolean ok = auth.loginMerchant(phone, pwd);
        if (ok) {
            currentMerchant = merchantDAO.findByPhone(phone);
            System.out.println("商家登录成功，进入商家菜单");
            merchantMenu(sc);
        } else {
            System.out.println("商家登录失败（检查手机号/密码或是否被封禁）");
        }
    }

    private static void adminLogin(Scanner sc) throws SQLException {
        System.out.print("管理员用户名: ");
        String name = sc.nextLine();
        System.out.print("密码: ");
        String pwd = sc.nextLine();
        boolean ok = auth.loginAdmin(name, pwd);
        if (ok) {
            adminUser = name;
            System.out.println("管理员登录成功，进入管理员菜单");
            adminMenu(sc);
        } else {
            System.out.println("管理员登录失败");
        }
    }

    // ---------- 用户菜单 (登录后) ----------
    private static void userMenu(Scanner sc) throws SQLException {
        while (true) {
            System.out.println("用户菜单：1 列出商品(可购买/私信/加入购物车) 2 搜索商品 3 查看消息 4 切换到商家 5 领取优惠券 6 加入购物车 0 注销");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1":
                    List<Product> list = productService.listPublished();
                    for (Product p : list)
                        System.out.println(p.getProductId() + " | " + p.getTitle() + " | " + p.getPrice() + " | 库存:" + p.getStock() + " | 联系方式: 使用站内私信或购买后交换");
                    // 浏览时提供购买或私信操作（购买只能在浏览界面触发）
                    System.out.print("输入要操作的商品ID（或回车返回）: ");
                    String sel = sc.nextLine().trim();
                    if (!sel.isEmpty()) {
                        Product prod = productDAO.findById(sel);
                        if (prod == null) { System.out.println("商品不存在"); break; }
                        System.out.println("1 购买 2 私信卖家 3 举报 4 加入购物车 其它 返回");
                        String act = sc.nextLine().trim();
                        if (act.equals("1")) {
                            System.out.print("数量: ");
                            int qty = Integer.parseInt(sc.nextLine());
                            double total = prod.getPrice() * qty;
                            // 提示用户可使用优惠券
                            java.util.List<String> coupons = productService.listUserCoupons(currentUserPhone);
                            String useCouponId = null;
                            if (coupons != null && !coupons.isEmpty()) {
                                System.out.println("您已领取的优惠券:");
                                for (String uc : coupons) System.out.println(uc);
                                System.out.print("输入要使用的 user_coupon_id (或回车跳过): ");
                                String chosen = sc.nextLine().trim();
                                if (!chosen.isEmpty()) useCouponId = chosen;
                            }
                            // 减少库存并创建订单（若使用优惠券则调用带券的下单）
                            productDAO.reduceStock(sel, qty);
                            if (useCouponId == null) {
                                orderService.createOrder(currentUserPhone, prod.getMerchantId(), total, 0.0, 0.0);
                            } else {
                                orderService.createOrderWithCoupon(currentUserPhone, prod.getMerchantId(), total, useCouponId);
                            }
                            System.out.println("购买成功，已创建订单");
                            messageService.sendContactExchange(currentUserPhone, prod.getMerchantPhone());
                        } else if (act.equals("2")) {
                            System.out.print("输入给卖家的消息（勿发明文联系方式）: ");
                            String content = sc.nextLine();
                            messageService.sendMessagePublic(currentUserPhone, prod.getMerchantPhone(), content);
                            System.out.println("消息已发送");
                        } else if (act.equals("3")) {
                            // 举报流程：可举报商家或商品
                            System.out.println("举报选项：1 举报商家 2 举报商品 其它 取消");
                            String which = sc.nextLine().trim();
                            if (which.equals("1") || which.equals("2")) {
                                System.out.println("请选择举报类型：1 服务 2 质量 3 欺诈");
                                String t = sc.nextLine().trim();
                                Enums.ComplaintType type = Enums.ComplaintType.SERVICE;
                                if ("2".equals(t)) type = Enums.ComplaintType.QUALITY;
                                else if ("3".equals(t)) type = Enums.ComplaintType.FRAUD;
                                String target = which.equals("1") ? prod.getMerchantId() : prod.getProductId();
                                boolean ok = complaintService.submitComplaint(currentUserPhone, target, type);
                                System.out.println(ok ? "举报提交成功，管理员将会处理" : "举报提交失败，请稍后重试");
                            } else {
                                System.out.println("已取消举报");
                            }
                        } else if (act.equals("4")) {
                            System.out.print("数量: ");
                            int qty = Integer.parseInt(sc.nextLine());
                            try {
                                // TODO: addToCart 实现仍为占位，后续需实现持久化或内存原型
                                cartService.addToCart(currentUserPhone, sel, qty);
                                System.out.println("已添加至购物车（若该接口被实现）。");
                            } catch (UnsupportedOperationException e) {
                                System.out.println("该功能尚未实现。接口已存在。");
                            }
                        }
                    }
                    break;
                case "6":
                    // 简单入口：让用户通过商品 ID 与数量将商品加入购物车（调用接口）
                    System.out.print("输入要加入购物车的商品ID: ");
                    String pid = sc.nextLine().trim();
                    if (pid.isEmpty()) { System.out.println("已取消"); break; }
                    System.out.print("数量: ");
                    int q = Integer.parseInt(sc.nextLine());
                    try {
                        // TODO: addToCart 为占位实现，应在后续迭代中实现购物车持久化和 checkout
                        cartService.addToCart(currentUserPhone, pid, q);
                        System.out.println("已添加至购物车（若该接口被实现）。");
                    } catch (UnsupportedOperationException e) {
                        System.out.println("该功能尚未实现。接口已存在。");
                    }
                    break;
                case "2":
                    System.out.print("关键词: ");
                    String kw = sc.nextLine();
                    List<Product> sres = productService.searchProducts(kw);
                    for (Product p : sres)
                        System.out.println(p.getProductId() + " | " + p.getTitle() + " | " + p.getPrice() + " | 库存:" + p.getStock() + " | 联系方式: 使用站内私信或购买后交换");
                    break;
                case "3":
                    List<String> msgs = messageService.getMessagesFor(currentUserPhone);
                    if (msgs.isEmpty()) System.out.println("无消息"); else msgs.forEach(System.out::println);
                    // 支持查看会话并在会话中举报对方
                    System.out.print("输入要查看会话的对方ID(手机号/商家ID)，或回车返回: ");
                    String other = sc.nextLine().trim();
                    if (!other.isEmpty()) {
                        java.util.List<String> conv = messageService.getConversation(currentUserPhone, other);
                        conv.forEach(System.out::println);
                        System.out.print("在此会话中举报对方？(y/N): ");
                        String rep = sc.nextLine().trim();
                        if (rep.equalsIgnoreCase("y")) {
                            System.out.println("请选择举报类型：1 服务 2 质量 3 欺诈");
                            String t = sc.nextLine().trim();
                            Enums.ComplaintType type = Enums.ComplaintType.SERVICE;
                            if ("2".equals(t)) type = Enums.ComplaintType.QUALITY;
                            else if ("3".equals(t)) type = Enums.ComplaintType.FRAUD;
                            boolean ok = complaintService.submitComplaint(currentUserPhone, other, type);
                            System.out.println(ok ? "举报提交成功，管理员将会处理" : "举报提交失败，请稍后重试");
                        }
                    }
                    break;
                case "4":
                    // 切换为商家：若当前手机号已有商家账户则直接登录，否则创建一个简单商家并登录
                    com.marketplace.models.Merchant m = merchantDAO.findByPhone(currentUserPhone);
                    if (m == null) {
                        System.out.print("输入店铺名以创建商家账户: ");
                        String shop = sc.nextLine();
                        m = new com.marketplace.models.Merchant(shop, currentUserPhone, "", com.marketplace.models.Enums.IDENTITY.BOSS);
                        merchantDAO.save(m, "");
                        System.out.println("已为当前用户创建商家账户并切换到商家模式");
                    } else {
                        System.out.println("检测到已有商家账户，已切换到商家模式");
                    }
                    currentMerchant = merchantDAO.findByPhone(currentUserPhone);
                    merchantMenu(sc);
                    break;
                case "5":
                    System.out.print("输入商家 ID 以查看其优惠券: ");
                    String mid = sc.nextLine();
                    java.util.List<String> coupons = productService.listCouponsForMerchant(mid);
                    if (coupons == null || coupons.isEmpty()) System.out.println("无可领取的优惠券或商家不存在");
                    else {
                        for (String cc : coupons) System.out.println(cc);
                        System.out.print("输入要领取的 coupon_id (或回车取消): ");
                        String chosen = sc.nextLine().trim();
                        if (!chosen.isEmpty()) {
                            boolean ok = productService.claimCoupon(chosen, currentUserPhone);
                            System.out.println(ok ? "领取成功" : "领取失败（可能已发完）");
                        }
                    }
                    break;
                case "0":
                    currentUserPhone = null;
                    System.out.println("已退出用户菜单");
                    return;
                default:
                    System.out.println("未知命令");
            }
        }
    }

    // ---------- 商家菜单 (登录后) ----------
    private static void merchantMenu(Scanner sc) throws SQLException {
        while (true) {
            System.out.println("商家菜单：1 发布商品 2 列出我的商品 3 查看消息 4 私信用户 0 注销");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1":
                    System.out.print("标题: ");
                    String title = sc.nextLine();
                    System.out.print("描述: ");
                    String desc = sc.nextLine();
                    System.out.print("价格: ");
                    double price = Double.parseDouble(sc.nextLine());
                    System.out.print("库存: ");
                    int stock = Integer.parseInt(sc.nextLine());
                    productService.publishProduct(title, desc, price, stock, currentMerchant.getMerchantId(), currentMerchant.getPhone());
                    System.out.println("发布成功");
                    break;
                case "2":
                    List<Product> my = productDAO.listByMerchant(currentMerchant.getMerchantId());
                    for (Product p : my)
                        System.out.println(p.getProductId() + " | " + p.getTitle() + " | " + p.getPrice() + " | 库存:" + p.getStock());
                    break;
                case "5":
                    // 创建优惠券
                    System.out.print("优惠码: ");
                    String code = sc.nextLine();
                    System.out.print("折扣金额(直接减免): ");
                    double d = Double.parseDouble(sc.nextLine());
                    System.out.print("有效期(可留空): ");
                    String until = sc.nextLine();
                    System.out.print("总发行数量: ");
                    int qty = Integer.parseInt(sc.nextLine());
                    productService.createCoupon(currentMerchant.getMerchantId(), code, d, until, qty);
                    System.out.println("已创建优惠券: " + code);
                    break;
                case "3":
                    List<String> msgs = messageService.getMessagesFor(currentMerchant.getPhone());
                    if (msgs.isEmpty()) System.out.println("无新消息"); else msgs.forEach(System.out::println);
                    break;
                case "4":
                    System.out.print("目标用户手机号: ");
                    String to = sc.nextLine();
                    System.out.print("内容: ");
                    String content = sc.nextLine();
                    messageService.sendMessagePublic(currentMerchant.getPhone(), to, content);
                    System.out.println("已发送");
                    break;
                case "0":
                    currentMerchant = null;
                    System.out.println("商家已注销");
                    return;
                default:
                    System.out.println("未知命令");
            }
        }
    }

    // ---------- 管理员菜单 (登录后) ----------
    private static void adminMenu(Scanner sc) throws SQLException {
        while (true) {
            System.out.println("管理员菜单：1 搜索商品 2 查看所有用户手机号 3 查看被封商品 4 查看被封手机号 5 封禁商品 6 封禁手机号 7 取消封禁 8 强制删除商品 9 一键清空数据 10 加载样例数据 0 注销");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1":
                    System.out.print("关键词: ");
                    String kw = sc.nextLine();
                    List<Product> sres = productService.searchProducts(kw);
                    for (Product p : sres)
                        System.out.println(p.getProductId() + " | " + p.getTitle() + " | " + p.getPrice() + " | 库存:" + p.getStock());
                    break;
                case "2":
                    java.util.List<String> phones = adminService.listAllUserPhonesSorted();
                    if (phones.isEmpty()) System.out.println("无用户"); else phones.forEach(System.out::println);
                    break;
                case "3":
                    java.util.List<String> bp = adminService.listBannedProductsSorted();
                    if (bp.isEmpty()) System.out.println("无被封商品"); else bp.forEach(System.out::println);
                    break;
                case "4":
                    java.util.List<String> bphones = adminService.listBannedPhonesSorted();
                    if (bphones.isEmpty()) System.out.println("无被封手机号"); else bphones.forEach(System.out::println);
                    break;
                case "5":
                    System.out.print("商品ID: ");
                    String pidBan = sc.nextLine();
                    adminService.banProduct(pidBan, "管理员封禁 by " + adminUser);
                    System.out.println("商品已封禁 " + pidBan);
                    break;
                case "6":
                    System.out.print("手机号: ");
                    String bp2 = sc.nextLine();
                    adminService.banPhone(bp2, "管理员操作 by " + adminUser);
                    System.out.println("已封禁 " + bp2);
                    break;
                case "7":
                    System.out.print("手机号: ");
                    String ub = sc.nextLine();
                    adminService.unbanPhone(ub);
                    System.out.println("已解禁 " + ub);
                    break;
                case "8":
                    System.out.print("商品ID: ");
                    String pid = sc.nextLine();
                    adminService.forceDeleteProduct(pid);
                    System.out.println("商品已删除 " + pid);
                    break;
                case "9":
                    System.out.print("确认清空所有主要数据？这将删除用户/商品/订单等数据，仍保留管理员账号。输入 YES 确认: ");
                    String conf = sc.nextLine();
                    if ("YES".equals(conf)) {
                        adminService.clearAllData();
                        System.out.println("已清空主要数据");
                    } else System.out.println("已取消");
                    break;
                case "10":
                    adminService.seedSampleData();
                    System.out.println("已加载样例商家与商品（若不存在）");
                    break;
                case "0":
                    adminUser = null;
                    System.out.println("管理员已退出");
                    return;
                default:
                    System.out.println("未知命令");
            }
        }
    }
}
