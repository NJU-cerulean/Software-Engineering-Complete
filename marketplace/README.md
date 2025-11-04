# Marketplace - UML-based Implementation

这是一个基于你提供 UML 图的简易市场系统实现（Java + SQLite）。

功能亮点：
- 用户/商家/管理员模型实现
- 使用 SQLite 持久化账号、商品与订单
- 简单的控制台交互演示注册、登录、发布商品、下单、管理员封禁/解封

运行：
- 需要 JDK 11+ 与 Maven
- 在项目根目录运行：

```powershell
mvn package
java -cp target\marketplace-1.0-SNAPSHOT.jar com.marketplace.Main
```

数据库文件 `marketplace.db` 会在运行时在项目根目录创建。

说明：该实现侧重后端逻辑与持久化，尽量遵循 Java 代码风格并使用 DAO 分层。
