<p align="center">
	<a href="https://wsido.top/" target="_blank">
		<img src="./pic/NBlog.png" alt="NBlog logo" style="width: 200px; height: 200px">
	</a>
</p>
<p align="center">
	<img src="https://img.shields.io/badge/JDK-1.8+-orange">
	<img src="https://img.shields.io/badge/SpringBoot-2.2.7.RELEASE-brightgreen">
	<img src="https://img.shields.io/badge/MyBatis-3.5.5-red">
	<img src="https://img.shields.io/badge/Vue-2.6.11-brightgreen">
	<img src="https://img.shields.io/badge/license-MIT-blue">
	<img src="https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2Fwsido%2FNBlog&count_bg=%2344CC11&title_bg=%23555555&icon=notist.svg&icon_color=%231296DB&title=hits&edge_flat=false">
</p>


## 简介

NBlog 是一个基于 Spring Boot + Vue 技术栈实现的「前后端分离」博客系统。

个人自用博客，长期维护，欢迎交流与勘误。

**预览地址：**

*   前台：[https://wsido.top](https://wsido.top)
*   后台管理：[https://admin.wsido.top](https://admin.wsido.top) (默认账户: Admin / 123456)

## 项目结构

```
NBlog
├── blog-api         # 后端API服务 (Spring Boot)
├── blog-cms         # 后台管理系统 (Vue2 + Element UI)
├── blog-view        # 前台博客展示 (Vue2 + Semantic UI + Element UI)
├── pic              # 项目相关图片资源
├── LICENSE          # 开源许可证
└── README.md        # 项目说明文档
```
其他根目录下的 `.md` 文件（如 `NBlog毕业论文.md`, `系统功能.md` 等）包含了项目的设计思路、功能描述等补充信息，可供参考。

## 技术栈

### 后端 (`blog-api`)

*   核心框架：[Spring Boot](https://github.com/spring-projects/spring-boot) 2.2.7.RELEASE
*   安全框架：[Spring Security](https://github.com/spring-projects/spring-security)
*   Token认证：[jjwt](https://github.com/jwtk/jjwt)
*   ORM 框架：[MyBatis](https://github.com/mybatis/spring-boot-starter) 3.5.5
*   分页插件：[PageHelper](https://github.com/pagehelper/Mybatis-PageHelper)
*   NoSQL 缓存：[Redis](https://github.com/redis/redis)
*   Markdown 转 HTML：[commonmark-java](https://github.com/commonmark/commonmark-java)
*   离线 IP 地址库：[ip2region](https://github.com/lionsoul2014/ip2region)
*   定时任务：[Quartz](https://github.com/quartz-scheduler/quartz)
*   UserAgent 解析：[yauaa](https://github.com/nielsbasjes/yauaa)
*   邮件模板参考自 [Typecho-CommentToMail-Template](https://github.com/MisakaTAT/Typecho-CommentToMail-Template)

### 前端

#### 通用

*   核心框架：Vue 2.6.11, Vue Router, Vuex
*   构建工具：@vue/cli 4.x
*   主要依赖：[axios](https://github.com/axios/axios), [moment](https://github.com/moment/moment), [nprogress](https://github.com/rstacruz/nprogress), [lodash](https://github.com/lodash/lodash), [prismjs](https://github.com/PrismJS/prism)

#### 后台管理系统 (`blog-cms`)

*   基于 [vue-admin-template](https://github.com/PanJiaChen/vue-admin-template) 二次修改后的 [my-vue-admin-template](https://github.com/wsido/my-vue-admin-template) 模板开发（[于2021年11月1日重构](https://github.com/wsido/NBlog/commit/b33641fe34b2bed34e8237bacf67146cd64be4cf)）
*   UI 框架：[Element UI](https://github.com/ElemeFE/element)
*   富文本编辑器：[mavonEditor](https://github.com/hinesboy/mavonEditor)
*   图表：[echarts](https://github.com/apache/echarts)

#### 前台博客 (`blog-view`)

*   UI 框架：
    *   [Semantic UI](https://semantic-ui.com/)：主要负责页面布局与整体风格。
    *   [Element UI](https://github.com/ElemeFE/element)：用于部分组件，作为 Semantic UI 的补充。
*   文章排版：基于 [typo.css](https://github.com/sofish/typo.css) 修改。
*   图片预览：[v-viewer](https://github.com/fengyuanchen/viewerjs)
*   音乐播放器：[APlayer](https://github.com/DIYgod/APlayer) + [MetingJS](https://github.com/metowolf/MetingJS)
*   文章目录：[tocbot](https://github.com/tscanlin/tocbot)
*   CSS效果参考：[iCSS](https://github.com/chokcoco/iCSS)

**由 [@willWang8023](https://github.com/willWang8023) 维护的 Vue3 版本请查看 [blog-view-vue3](https://github.com/willWang8023/blog-view-vue3)**

## 文档说明

### 开发文档
项目包含一些辅助开发的 Markdown 文档，位于项目根目录，例如：
*   `NBlog毕业论文.md`: 可能包含详细的需求分析、系统设计等。
*   `系统功能.md`: 系统主要功能模块描述。
*   `系统实现建议.md`: 关于系统实现的一些建议和思考。

建议开发者在开始工作前阅读这些文档以更好地理解项目。

### API 文档
目前项目暂未集成自动化的 API 文档生成工具（如 Swagger/OpenAPI）。
*   **强烈建议** 后续在 `blog-api` 模块中集成此类工具，以便自动生成和维护 API 文档，这将极大地帮助新成员理解后端接口并方便二次开发。
*   当前阶段，开发者需要通过阅读 `blog-api/src/main/java/top/wsido/controller/` 目录下的相关 Controller 代码来理解 API 接口定义。

### 用户手册
本项目目前没有提供独立的用户手册。`README.md` (本文档) 包含了项目的基本介绍、部署和使用说明，可作为基本的用户指引。后台管理系统 (`blog-cms`) 的操作相对直观，用户可以通过实际操作熟悉。

## 开发环境搭建

1.  **数据库准备**:
    *   创建 MySQL 数据库，名称为 `nblog`。
    *   **重要**: 确保数据库字符集为 `utf8mb4`，以支持 emoji 等特殊字符。
    *   执行 `/blog-api/nblog.sql` 脚本初始化表结构和基础数据。
2.  **后端配置**:
    *   修改后端配置文件 `/blog-api/src/main/resources/application-dev.properties`，根据实际情况配置数据库连接、Redis 地址、邮箱服务等。
    *   **安全提示**: 务必修改 `token.secretKey` 以保证 Token 安全性。
    *   `spring.mail.host`及`spring.mail.port`的默认配置为阿里云邮箱，如使用其他邮箱服务商，请查询相应配置。
3.  **启动服务**:
    *   安装并启动 Redis 服务。
    *   启动后端 `blog-api` Spring Boot 应用。
4.  **前端启动**:
    *   分别进入 `blog-cms` 和 `blog-view` 目录。
    *   执行 `npm install` 安装项目依赖。
    *   执行 `npm run serve` 启动前端开发服务器。
5.  **（可选）部署**
    *   如需部署，注意将 `/blog-view/src/plugins/axios.js` 和 `/blog-cms/src/util/request.js` 中的 `baseURL` 修改为你的后端 API 服务的实际访问地址。

## 注意事项

*   **环境版本**: 尽量确保本地 Maven 和 NPM/Node.js 环境与项目开发时使用的版本一致或兼容，避免因依赖版本问题导致错误。建议查阅 `pom.xml` 和 `package.json` 中的版本信息。
*   **默认用户**: 数据库中后台管理的默认用户名和密码为 `Admin` / `123456`。修改密码功能未在前端页面提供，如需修改，可在 `top.wsido.util.HashUtils` 类的 `main` 方法中手动生成加密后的密码，然后更新到数据库用户表中。
*   **个性化配置**:
    *   大部分站点相关的个性化配置（如站点标题、首页大图等）可以在后台管理系统的 "站点设置" 菜单中进行修改。
    *   小部分配置（特别是影响首屏加载速度或特定静态资源的，如部分首页图片）可能需要直接修改前端项目的源码。

## 隐藏功能

*   **博主身份评论**: 在前台博客页面，访问 `/login` 路径并成功登录后，评论时将自动带有 "博主" 标识，且无需再次填写昵称和邮箱。
*   **文章内嵌音乐播放器**: 在 Markdown 文章中，使用以下格式添加代码（注意是正文形式，非代码块）：
    ```html
    <meting-js server="netease" type="song" id="你的歌曲ID" theme="#25CCF7"></meting-js>
    ```
    这将在文章中嵌入一个 [APlayer](https://github.com/DIYgod/APlayer) 音乐播放器。`netease` 代表网易云音乐，更多用法请参考 [MetingJS](https://github.com/metowolf/MetingJS) 文档。
*   **特殊文本效果**:
    *   黑幕效果：使用 `@@隐藏文字@@` 包裹文本，鼠标悬浮时显示。
    *   选中反色：使用 `%%隐藏文字%%` 包裹文本，文字将被蓝色覆盖，鼠标选中时反色显示。

## License

[MIT](https://github.com/wsido/NBlog/blob/master/LICENSE)

## Thanks

*   感谢 [JetBrains](https://www.jetbrains.com/?from=NBlog) 为本项目提供的 Open Source License。
*   感谢在技术栈中提到的所有优秀的开源项目。
