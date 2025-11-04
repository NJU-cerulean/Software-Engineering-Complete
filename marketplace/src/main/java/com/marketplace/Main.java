package com.marketplace;

import com.marketplace.models.Merchant;
import com.marketplace.models.User;
import com.marketplace.service.AdminService;
import com.marketplace.service.AuthService;
import com.marketplace.service.OrderService;
import com.marketplace.service.ProductService;
import com.marketplace.service.MessageService;
import com.marketplace.dao.ProductDAO;
import com.marketplace.dao.MerchantDAO;
import com.marketplace.dao.UserDAO;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * 程序入口：实现用户/商家/管理员三种角色的菜单与交互，满足：
 * - 只有登录后的用户可购买
 * - 只有登录后的商家可发布商品
 * - 购买成功后交换联系方式
 * - 私信功能（存入 DB）
 * - 管理员可以强制删除商品并封禁手机号
 */
public class Main {
    private static final AuthService auth = new AuthService();
    private static final ProductService productService = new ProductService();
    private static final OrderService orderService = new OrderService();
    private static final AdminService adminService = new AdminService();
    private static final MessageService messageService = new MessageService();
    private static final ProductDAO productDAO = new ProductDAO();
    private static final MerchantDAO merchantDAO = new MerchantDAO();
    private static final UserDAO userDAO = new UserDAO();

    // 会话状态
    private static String currentUserPhone = null; // 以手机号标识用户
    private static Merchant currentMerchant = null; // 登录的商家
    private static boolean adminLogged = false;
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
        System.out.println(ok ? "注册成功" : "注册失败（可能被封禁或已存在）");
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
            adminLogged = true;
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
            System.out.println("用户菜单：1 列出商品 2 搜索商品 3 购买商品 4 查看消息 5 私信商家 0 注销");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1":
                    List<com.marketplace.models.Product> list = productService.listPublished();
                    for (com.marketplace.models.Product p : list)
                        System.out.println(p.getProductId() + " | " + p.getTitle() + " | " + p.getPrice() + " | 库存:" + p.getStock() + " | 联系方式: 使用站内私信或购买后交换");
                    break;
                case "2":
                    System.out.print("关键词: ");
                    String kw = sc.nextLine();
                    List<com.marketplace.models.Product> sres = productService.searchProducts(kw);
                    for (com.marketplace.models.Product p : sres)
                        System.out.println(p.getProductId() + " | " + p.getTitle() + " | " + p.getPrice() + " | 库存:" + p.getStock() + " | 联系方式: 使用站内私信或购买后交换");
                    break;
                case "3":
                    System.out.print("商品ID: ");
                    String pid = sc.nextLine();
                    System.out.print("数量: ");
                    int qty = Integer.parseInt(sc.nextLine());
                    com.marketplace.models.Product prod = productDAO.findById(pid);
                    if (prod == null) { System.out.println("商品不存在"); break; }
                    // 先检查库存并减少
                    productDAO.reduceStock(pid, qty);
                    double total = prod.getPrice() * qty;
                    orderService.createOrder(currentUserPhone, prod.getMerchantId(), total, 0.0, 0.0);
                    System.out.println("购买成功，已创建订单");
                    // 通过受控的站内消息交换联系方式（只发送掩码）
                    messageService.sendContactExchange(currentUserPhone, prod.getMerchantPhone());
                    break;
                case "4":
                    List<String> msgs = messageService.getMessagesFor(currentUserPhone);
                    if (msgs.isEmpty()) System.out.println("无新消息"); else msgs.forEach(System.out::println);
                    break;
                case "5":
                    System.out.print("目标商家手机号: ");
                    String to = sc.nextLine();
                    System.out.print("内容: ");
                    String content = sc.nextLine();
                    messageService.sendMessagePublic(currentUserPhone, to, content);
                    System.out.println("已发送");
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
                    List<com.marketplace.models.Product> my = productDAO.listByMerchant(currentMerchant.getMerchantId());
                    for (com.marketplace.models.Product p : my)
                        System.out.println(p.getProductId() + " | " + p.getTitle() + " | " + p.getPrice() + " | 库存:" + p.getStock());
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
            System.out.println("管理员菜单：1 搜索商品 2 查看所有用户手机号 3 查看被封商品 4 查看被封手机号 5 封禁商品 6 封禁手机号 7 取消封禁 8 强制删除商品 0 注销");
            String c = sc.nextLine().trim();
            switch (c) {
                case "1":
                    System.out.print("关键词: ");
                    String kw = sc.nextLine();
                    List<com.marketplace.models.Product> sres = productService.searchProducts(kw);
                    for (com.marketplace.models.Product p : sres)
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
                case "0":
                    adminLogged = false;
                    adminUser = null;
                    System.out.println("管理员已退出");
                    return;
                default:
                    System.out.println("未知命令");
            }
        }
    }
}
