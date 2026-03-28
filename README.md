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

## 🛠️ 技术栈

### 后端技术
- **Spring Boot 2.7.18** - Java后端框架
- **Java 8** - 编程语言
- **Maven** - 依赖管理和构建工具
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

- 💰 **账户管理**：支付宝余额和余额宝账户
- 🔄 **资金转移**：余额与余额宝之间的转入转出
- 📈 **自动收益**：余额宝每日自动计算收益
- 📱 **手机访问**：生成二维码支持手机扫码访问
- 🎨 **界面仿照**：高度仿照手机版支付宝和余额宝界面

## 🔧 项目结构

```
smallYuEBao/
├── backend/                 # 后端Spring Boot项目
│   ├── src/main/java/      # Java源代码
│   ├── pom.xml            # Maven配置
│   └── target/            # 构建输出
├── frontend/               # 前端React项目
│   ├── src/
│   │   ├── pages/         # 页面组件
│   │   ├── services/      # API服务
│   │   └── App.js         # 主应用
│   ├── public/            # 静态资源
│   └── package.json       # 依赖配置
├── Dockerfile             # Docker构建配置
├── nginx.conf             # Nginx配置
├── build.bat              # Windows构建脚本
└── README.md              # 项目说明
```

## 📞 API接口

- `GET /api/accounts` - 获取账户信息
- `POST /api/transfer/in` - 转入余额宝
- `POST /api/transfer/out` - 转出余额宝
- `GET /api/ip` - 获取服务器IP地址

## 🔒 注意事项

1. **手机访问**：确保手机和服务器在同一WiFi网络下
2. **防火墙**：可能需要开放80或3000端口
3. **IP地址**：Docker部署时需要正确设置服务器IP
4. **Java版本**：项目使用Java 8，确保环境兼容

