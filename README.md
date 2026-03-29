# 小余额宝项目

一个仿支付宝余额宝功能的Web应用，支持资金转入转出、自动收益计算和手机扫码访问。

## 🚀 快速开始

### 本地部署

如果有人clone项目后，输入以下指令即可在本地部署运行：

```bash
# 1. 构建后端
cd backend
mvn clean package -DskipTests

# 2. 启动后端服务（新开终端）
java -jar target/yuebao-backend-1.0.0.jar

# 3. 启动前端服务（新开终端）
cd frontend
npm install
npm start
```

访问地址：http://localhost:3000

### Docker部署

如果要用Docker部署，需要手动获取本机IP地址，并加在环境变量当中：

```bash
# 1. 获取本机IP地址（Windows）
ipconfig | findstr "IPv4"

# 2. 使用IP地址构建Docker镜像（替换为实际IP）
docker build --build-arg REACT_APP_SERVER_IP=10.6.38.81 -t small-yuebao .

# 3. 启动容器
docker run -d -p 80:80 --name yuebao-app small-yuebao
```


访问地址：http://localhost（电脑端）或 http://你的IP地址（手机端）

## 🔑 演示账户

项目内置了一个演示账户，可以直接登录体验完整功能：

- **用户名**: `demo`
- **密码**: `123456`

登录后可以看到：
- 支付宝余额：0.00元
- 余额宝余额：1000.25元（包含0.25元收益）
- 完整的转入转出功能
- 自动收益计算
- 交易记录查看

## 🛠️ 技术栈

### 后端技术
- **Spring Boot 2.7.18** - Java后端框架
- **Java 8** - 编程语言
- **Maven** - 依赖管理和构建工具
- **Spring Security + JWT** - 用户认证和授权
- **Spring Data JPA** - 数据持久化
- **H2 Database** - 内存数据库（开发环境）
- **RESTful API** - 提供账户管理和资金操作接口

### 前端技术
- **React** - 前端框架
- **Ant Design** - UI组件库
- **Axios** - HTTP客户端
- **QRCode.react** - 二维码生成
- **CSS3** - 样式设计，高度仿照支付宝/余额宝界面

### 部署技术
- **Docker** - 容器化部署
- **Nginx** - 反向代理和静态文件服务
- **多阶段构建** - 优化镜像大小

## 📱 主要功能

### 🔐 用户认证模块
- **用户注册**：支持新用户注册，包含用户名、密码、手机号、邮箱等信息
- **用户登录**：基于JWT的认证机制，支持持久化登录状态
- **安全加密**：密码使用BCrypt加密存储，确保数据安全
- **自动登录**：支持token自动登录，提升用户体验

### 💾 数据持久化
- **用户管理**：用户信息、账户信息、交易记录的完整CRUD操作
- **自动初始化**：系统启动时自动创建演示账户和初始数据
- **交易记录**：完整的资金操作历史记录
- **实时更新**：账户余额和交易记录的实时同步

### 💰 资金管理功能
- **账户管理**：支付宝余额和余额宝账户
- **资金转移**：余额与余额宝之间的转入转出
- **自动收益**：余额宝每日自动计算收益
- **交易历史**：完整的资金操作记录查询

### 📱 移动端支持
- **手机访问**：生成二维码支持手机扫码访问
- **响应式设计**：完美适配各种屏幕尺寸
- **CORS支持**：跨域访问配置，支持移动端API调用
- **实时同步**：前后端数据实时同步更新

### 🎨 界面设计
- **界面仿照**：高度仿照手机版支付宝和余额宝界面
- **主题切换**：支持支付宝蓝和余额宝橙两种主题
- **交互优化**：流畅的动画效果和用户交互体验

## 🔧 项目结构

```
smallYuEBao/
├── backend/                         # 后端Spring Boot项目
│   ├── src/main/java/
│   │   └── com/example/yuebao/
│   │       ├── config/             # 配置类（安全、数据初始化等）
│   │       ├── controller/         # REST控制器
│   │       ├── entity/             # 实体类（User、Account、Transaction）
│   │       ├── repository/         # 数据访问层
│   │       ├── service/            # 业务逻辑层
│   │       ├── util/               # 工具类（JWT、密码加密等）
│   │       └── YuebaoApplication.java
│   ├── src/main/resources/
│   │   ├── application.yml         # 应用配置（数据库、JWT等）
│   │   └── data.sql                # 初始数据（可选）
│   ├── pom.xml                     # Maven配置
│   └── target/                     # 构建输出
├── frontend/                       # 前端React项目
│   ├── src/
│   │   ├── pages/                  # 页面组件（登录、支付宝、余额宝）
│   │   ├── services/               # API服务（认证、账户操作）
│   │   └── App.js                  # 主应用
│   ├── public/                     # 静态资源
│   └── package.json                # 依赖配置
├── Dockerfile                      # Docker构建配置
├── nginx.conf                      # Nginx配置
├── build.bat                       # Windows构建脚本
└── README.md                       # 项目说明
```

### 💾 数据库设计

项目使用H2内存数据库，包含以下核心表：

- **users表**：用户信息（用户名、密码、手机号、邮箱等）
- **accounts表**：账户信息（支付宝余额、余额宝余额、收益等）
- **transactions表**：交易记录（操作类型、金额、时间等）

### 🔄 数据初始化

系统启动时自动创建演示数据：
- 演示用户：demo/123456
- 初始账户：支付宝500.00元，余额宝500.25元
- 自动收益计算：余额宝每日产生收益

## 📞 API接口

### 🔐 认证接口
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/login` - 用户登录
- `GET /api/auth/me` - 获取当前用户信息

### 💰 账户管理接口
- `GET /api/accounts` - 获取账户信息
- `POST /api/transfer/in` - 转入余额宝
- `POST /api/transfer/out` - 转出余额宝
- `GET /api/transactions` - 获取交易记录

### 🌐 系统接口
- `GET /api/ip` - 获取服务器IP地址

### 🔒 安全特性
- **JWT认证**：所有敏感接口都需要有效的JWT token
- **密码加密**：用户密码使用BCrypt加密存储
- **CORS配置**：支持跨域访问，便于移动端调用

## 🔒 注意事项

### 🚀 部署注意事项
1. **手机访问**：确保手机和服务器在同一WiFi网络下
2. **防火墙**：可能需要开放80或3000端口
3. **IP地址**：Docker部署时需要正确设置服务器IP
4. **Java版本**：项目使用Java 8，确保环境兼容

### 🔐 认证安全说明
1. **JWT Token**：登录后token存储在localStorage，退出时自动清除
2. **密码安全**：所有密码使用BCrypt加密，无法直接查看明文
3. **会话管理**：支持自动登录，token过期时间为24小时
4. **跨域安全**：CORS配置允许所有来源，生产环境建议限制

### 💾 数据库说明
1. **H2数据库**：开发环境使用内存数据库，重启后数据重置
2. **数据持久化**：如需持久化存储，可配置MySQL或PostgreSQL
3. **自动初始化**：每次启动自动创建演示账户，便于测试
4. **H2控制台**：开发时可访问 http://localhost:8080/h2-console 查看数据库

### 📱 移动端适配
1. **响应式设计**：完美适配手机、平板、桌面各种屏幕
2. **退出按钮优化**：手机端退出按钮位置经过特殊优化，避免遮挡
3. **二维码生成**：登录页面即可生成二维码，方便手机访问
4. **API兼容性**：前后端完全分离，支持多端访问