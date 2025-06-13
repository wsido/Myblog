# NBlog 个人博客系统

NBlog是一个功能完善的现代化个人博客系统，采用前后端分离架构设计，提供了博客管理、评论交互、动态发布、数据统计等全方位功能，满足个人博客建站的各种需求。

## 📋 功能特点

- **博客管理**：支持博客的发布、编辑、删除、分类、标签等管理功能
- **评论系统**：支持多级评论、回复、通知提醒等互动功能
- **动态管理**：支持发布、编辑、删除个人动态
- **敏感词过滤**：内置敏感词过滤功能，支持自定义敏感词和白名单
- **封面生成**：自动生成高质量博客封面图片
- **统计分析**：提供访问量、评论数等数据的统计和分析
- **移动端适配**：响应式设计，完美支持移动端访问
- **安全认证**：基于JWT的安全认证机制

## 🔧 技术栈

### 后端
- **框架**：Spring Boot
- **ORM**：MyBatis
- **数据库**：MySQL
- **缓存**：Redis
- **认证**：JWT (JSON Web Token)
- **构建工具**：Maven

### 前端
- **后台管理**：Vue.js + Element UI
- **前台展示**：Vue.js + 自定义组件
- **HTTP客户端**：Axios
- **构建工具**：Webpack

## 🏗️ 项目结构

项目分为三个主要部分：

- **blog-api**：后端API服务
- **blog-cms**：内容管理系统（CMS）
- **blog-view**：博客前台展示

## 🚀 快速开始

### 环境要求
- JDK 1.8+
- Maven 3.0+
- MySQL 5.7+
- Redis
- Node.js 12+

### 后端部署
1. 克隆仓库
   ```
   git clone https://github.com/yourusername/NBlog.git
   cd NBlog
   ```

2. 配置数据库
   - 创建数据库 `nblog`
   - 配置 `blog-api/src/main/resources/application-dev.properties` 中的数据库连接信息

3. 启动后端服务
   ```
   cd blog-api
   mvn spring-boot:run
   ```

### 前端部署
1. CMS管理系统
   ```
   cd blog-cms
   npm install
   npm run serve
   ```

2. 博客前台
   ```
   cd blog-view
   npm install
   npm run serve
   ```

## 🖼️ 功能预览

### 管理后台
![管理后台](https://example.com/cms-preview.png)

### 博客前台
![博客前台](https://example.com/blog-preview.png)

## 🧩 核心功能详解

### 1. 博客管理
支持Markdown编辑器，自定义分类和标签，支持定时发布和私密博客。

### 2. 敏感词过滤
基于DFA算法的高性能敏感词过滤，支持自定义敏感词库和白名单。

### 3. 封面自动生成
基于模板的封面自动生成功能，支持自定义字体和样式。

## 📌 开发计划

- [ ] 集成第三方登录
- [ ] 增加博客导出功能
- [ ] 优化移动端体验
- [ ] 增强SEO支持

## 👨‍💻 贡献者

- [您的名字](https://github.com/yourusername) - 项目创建者与维护者

## 📄 许可证

本项目采用 MIT 许可证 - 详见 [LICENSE](LICENSE) 文件