# 人工智能学院研究生管理信息系统

> **Graduate Student Management Information System — School of Artificial Intelligence**

<div align="center">

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.4.31-4FC08D?style=flat-square&logo=vue.js)](https://vuejs.org/)
[![JDK](https://img.shields.io/badge/JDK-17-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7.0-DC382D?style=flat-square&logo=redis)](https://redis.io/)
[![License](https://img.shields.io/badge/License-Educational-blue?style=flat-square)](#)

</div>

---

## 📖 项目简介

本系统为 **哈尔滨学院人工智能学院** 研究生管理信息系统（本科毕业设计作品），旨在为二级学院构建一套覆盖研究生**从入学到毕业全生命周期**的信息化管理平台，解决传统人工管理模式下效率低、流程不规范、数据不互通等痛点。

系统采用 **B/S 前后端分离架构**，涵盖师生双选、学籍管理、课程管理、学术管理、论文管理、学位管理六大核心业务模块，支持学生、导师、教学秘书、分管院长、学位分委会主席、综合管理员六种用户角色的协同工作。

---

## 🚀 核心功能

系统按业务领域划分为 **六大核心模块**，各模块之间通过角色权限和数据流形成有机整体：

### 1. 师生双选管理 👥
- 学生填报 1-3 个志愿、导师反选确认
- 多轮次推进机制、补选轮次、手动分配
- 更换导师申请与审批
- 双选进度实时看板与通知推送

### 2. 学籍管理 📋
- 学生基本信息维护与批量导入（Excel）
- 学籍异动管理：**休学 / 复学 / 退学 / 延期毕业**
- 二级审批流程：导师初审 → 教学秘书终审
- 异动历史追溯与统计报表

### 3. 课程管理 📚
- 课程信息维护与排课管理
- 学生选课与个人课表查询
- 成绩录入与加权计算（平时 × 0.3 + 期末 × 0.7）
- 五档等级自动判定（优秀 / 良好 / 中等 / 及格 / 不及格）
- 教学评价

### 4. 学术管理 🎓
- 三类学术内容：**学术活动 / 学术成果 / 创新创业项目**
- 三级审批流程：导师 → 教学秘书 → 分管院长
- 主子表事务性设计，确保数据一致性
- 附件上传（七牛云对象存储）

### 5. 论文管理 📝
- 完整七环节串行管理：
  选题 → 任务书 → 开题报告 → 中期检查 → 过程稿 → 答辩稿 → 毕业论文
- 三级审批链路（导师 → 教学秘书 → 分管院长）
- 流程配置动态开关，灵活控制各环节启停
- 论文文件上传、在线预览与版本管理

### 6. 学位管理 🏅
- 前置条件校验：学分达标 + 论文答辩完成
- 学位申请 → 分委会审批 → 学位授予 → 毕业审核
- 自动 / 手动毕业审核双模式
- 学位证书编号管理

### 用户角色体系

| 角色 | 主要职责 |
|------|---------|
| **学生** | 志愿填报、选课、学术提交、论文提交、学位申请 |
| **导师** | 双选反选、学籍异动初审、学术审批、论文各环节审批 |
| **教学秘书** | 学籍异动终审、课程排课、成绩管理、学术终审、论文终审 |
| **分管院长** | 学术三级审批、论文关键环节审批 |
| **学位分委会主席** | 学位申请审批、学位授予确认 |
| **综合管理员** | 系统配置、用户管理、流程开关、数据统计 |

---

## 🛠️ 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | JDK 17 | 编程语言 |
| Spring Boot | 3.4.4 | 主框架，提供自动配置与起步依赖 |
| MyBatis-Plus | 3.5.5 | ORM 框架，简化 CRUD + 分页 |
| MyBatis | 3.0.4 | SQL 映射 |
| MySQL | 8.0 | 关系型数据库（腾讯云 CDB） |
| Redis | 7.0 | 缓存 / Token 鉴权存储（Jedis 客户端） |
| HikariCP | — | 数据库连接池（Spring Boot 内置） |
| JWT | 4.4.0 (java-jwt) | 无状态 Token 生成与校验 |
| FastJSON | 2.0.53 | JSON 序列化 / 反序列化 |
| 七牛云 SDK | 7.15.1 | 对象存储（文件 / 头像 / 学术附件上传） |
| EasyExcel | 3.3.2 | Excel 导入导出 |
| Commons CSV | 1.12.0 | CSV 文件解析 |
| Kaptcha | 2.3.2 | 图形验证码 |
| Spring AOP | 内置 | 面向切面编程（操作日志） |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.31 | 渐进式前端框架（Composition API） |
| Vite | 5.3.2 | 极速构建工具 |
| Element Plus | 2.7.6 | 企业级 UI 组件库 |
| Pinia | 2.1.7 | 轻量级状态管理 |
| Vue Router | 4.4.0 | 路由管理 |
| Axios | 0.28.1 | HTTP 客户端 |
| ECharts | 5.5.1 | 数据可视化图表 |
| SCSS/Sass | 1.77.5 | CSS 预处理器 |
| js-cookie | 3.0.5 | Cookie 管理 |
| NProgress | 0.2.0 | 页面加载进度条 |
| pdfjs-dist | 3.11.174 | PDF 在线预览 |
| @vueup/vue-quill | 1.2.0 | 富文本编辑器 |
| vue-cropper | 1.1.1 | 图片裁剪上传 |
| vuedraggable | 4.1.0 | 拖拽排序组件 |
| jsencrypt | 3.3.2 | 前端数据加密 |

> 💡 前端项目基于 **若依 (RuoYi) 3.8.9** 管理系统框架二次开发。

---

## 🏗️ 系统架构

系统采用 **四层 B/S 前后端分离架构**，遵循高内聚低耦合的设计原则：

```
┌─────────────────────────────────────────────────────┐
│                     业务前台                          │
│   Vue3 + Element Plus 统一登录入口                    │
│   根据角色动态加载菜单与权限                           │
├─────────────────────────────────────────────────────┤
│                     业务中台                          │
│  ┌──────┬──────┬──────┬──────┬──────┬──────┐        │
│  │双选管理│学籍管理│课程管理│学术管理│论文管理│学位管理│        │
│  └──────┴──────┴──────┴──────┴──────┴──────┘        │
├─────────────────────────────────────────────────────┤
│                    基础服务层                         │
│  JWT+Redis 鉴权 · 操作日志 AOP · SSE 实时推送          │
│  系统管理 · 定时任务 · 全局异常处理                     │
├─────────────────────────────────────────────────────┤
│                     存储层                           │
│  MySQL 业务数据 · Redis 缓存/Token · 七牛云 附件存储    │
└─────────────────────────────────────────────────────┘
```

### 安全机制
- **JWT + Redis 双重鉴权**：Token 有效期 3 小时，Redis 存储实现主动失效
- **Spring Interceptor**：拦截所有请求，白名单放行（`/login`、`/register`、`/captchaImage` 等）
- **Kaptcha 图形验证码**：登录防刷
- **密码哈希存储**：支持明文密码自动升级
- **前端路由守卫**：未登录自动跳转
- **后端动态路由**：根据角色 ID 实时加载菜单树

### 特色技术实现
- **SSE (Server-Sent Events)**：实时通知推送，支持 EventSource URL 参数传递 Token
- **AOP 操作日志**：`@Log` 注解自动记录用户操作行为，低侵入
- **雪花 ID 安全序列化**：超出 `Number.MAX_SAFE_INTEGER` 的 Long 值传输时自动转字符串
- **定时任务**：`@EnableScheduling` 支持通知定时推送与数据归档

---

## 📂 项目结构

```
GraduationProject22023237-openclaw/
│
├── 📁 gp22023237-server/               # ★ 后端 Spring Boot 项目
│   ├── pom.xml                         # Maven 配置
│   └── src/main/
│       ├── java/com/jameshao/gp22023237/
│       │   ├── GP22023237Application.java  # 启动类
│       │   ├── annotation/             # @Log 自定义注解
│       │   ├── aspect/                 # OperLogAspect 操作日志切面
│       │   ├── common/                 # 统一返回 / 枚举 / 错误码
│       │   ├── config/                 # Redis / MyBatis-Plus / Kaptcha / 拦截器配置
│       │   ├── controller/             # 40 个 Controller
│       │   ├── DTO/                    # 37 个数据传输对象
│       │   ├── intercepter/            # JWT+Redis 鉴权拦截器
│       │   ├── mapper/                 # 35 个 MyBatis-Plus Mapper
│       │   ├── po/                     # 35 个实体类
│       │   ├── service/                # 35 个接口 + 35 个实现
│       │   └── utils/                  # 14 个工具类
│       └── resources/
│           ├── application.properties  # 数据库 / Redis / 七牛云配置
│           └── mapper/                 # 22 个 MyBatis XML 映射
│
├── 📁 gp22023237-ui/                   # ★ 前端 Vue3 项目
│   ├── package.json                    # 依赖配置
│   ├── vite.config.js                  # Vite 构建配置（端口 89）
│   └── src/
│       ├── main.js                     # 入口文件
│       ├── permission.js               # 路由守卫
│       ├── api/                        # 43 个 API 模块
│       ├── assets/                     # 静态资源（样式/图标/图片）
│       ├── components/                 # 公共组件
│       ├── layout/                     # 布局组件（侧边栏/导航/标签页）
│       ├── router/                     # 路由配置（常量+动态路由）
│       ├── store/modules/              # Pinia 状态管理（6 个模块）
│       ├── utils/                      # 工具函数
│       └── views/                      # 页面视图
│           ├── index.vue               # 首页仪表盘
│           ├── login.vue               # 登录页
│           ├── register.vue            # 注册页
│           ├── academic/               # 学术管理页面
│           ├── course/                 # 课程管理页面
│           ├── degree/                 # 学位/论文管理页面（19 个子目录）
│           ├── selection/              # 师生双选页面（10 个子目录）
│           ├── student/                # 学籍管理页面（5 个子目录）
│           ├── system/                 # 系统管理页面
│           ├── monitor/                # 系统监控页面
│           └── tool/                   # 工具页面
└── 📁 sql/                             # 数据库脚本
    ├── gp22023237.sql                  # 完整数据库（含示例数据）
    └── gp22023237（仅结构）.sql         # 仅表结构
```

---

## 🚀 快速开始

### 环境要求

| 环境 | 最低版本 | 说明 |
|------|---------|------|
| JDK | 17+ | 后端运行环境 |
| Maven | 3.6+ | 后端构建工具 |
| MySQL | 8.0+ | 主数据库 |
| Redis | 7.0+ | 缓存与 Token 存储 |
| Node.js | 18+ | 前端运行环境（Vite 5 要求） |
| npm | 9+ | 前端包管理 |

> ⚠️ 七牛云账号为**可选**配置——仅文件上传功能需要，不影响系统核心功能运行。

### 1. 克隆项目

```bash
git clone https://github.com/JamesHa0/GraduationProject22023237.git
cd GraduationProject22023237
```

### 2. 初始化数据库

```mysql
-- 创建数据库
CREATE DATABASE gp22023237 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入表结构（从项目目录执行）
USE gp22023237;
SOURCE sql/gp22023237（仅结构）.sql;

-- 如需示例数据，改为导入完整版：
-- SOURCE sql/gp22023237.sql;
```

### 3. 配置后端

编辑 `gp22023237-server/src/main/resources/application.properties`：

```properties
# ========== 数据库配置 ==========
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/gp22023237?useUnicode=true&characterEncoding=utf-8
spring.datasource.username=你的数据库用户名
spring.datasource.password=你的数据库密码

# ========== Redis 配置 ==========
spring.data.redis.host=127.0.0.1
spring.data.redis.port=6379
spring.data.redis.password=你的Redis密码(如有)

# ========== 七牛云配置（可选） ==========
qiniu.accessKey=你的AccessKey
qiniu.secretKey=你的SecretKey
qiniu.bucket=你的Bucket名称
qiniu.domain=你的CDN域名
```

### 4. 启动后端

```bash
cd gp22023237-server/

# 方式一：Maven 命令行
mvn clean install -DskipTests
mvn spring-boot:run

# 方式二：IDE 中直接运行
# 运行 GP22023237Application.java 的 main 方法
```

后端启动后默认监听 **8088** 端口。控制台出现 `Started GP22023237Application` 即表示启动成功。

### 5. 启动前端

```bash
cd gp22023237-ui/

# 安装依赖（首次运行或依赖变更时）
npm install

# 启动开发服务器
npm run dev
```

前端启动后默认监听 **89** 端口，开发模式下 `/dev-api` 请求自动代理到 `http://localhost:8088`。

### 6. 访问系统

浏览器打开 **http://localhost:89** 进入登录页面：

1. 输入账号、密码
2. 输入图形验证码
3. 点击登录，系统根据角色自动加载对应菜单
4. 首次登录建议使用管理员账号，在系统管理中进行用户与角色配置

---

## 🌟 项目亮点

| 亮点 | 说明 |
|------|------|
| 🔐 **安全可靠** | JWT + Redis 双重鉴权，未过期 Token 可主动失效；Kaptcha 验证码防刷；密码哈希存储 |
| 📡 **实时通知** | SSE 长连接推送，审批进度、双选结果等消息实时触达 |
| 🔄 **复杂流程** | 师生双选多轮推进、论文七环节串行审批、学籍异动二级审批，状态机精确控制 |
| 📊 **数据可视化** | ECharts 仪表盘，直观展示各项统计数据 |
| 📝 **操作审计** | AOP 切面自动记录操作日志，`@Log` 注解低侵入 |
| 🧩 **主子表事务** | 学术管理采用主子表设计，保证提交数据一致性 |
| 🔌 **动态路由** | 根据角色 ID 从数据库加载菜单与权限，灵活可配 |
| 🎨 **若依框架** | 基于成熟的 RuoYi 3.8.9 框架，界面美观，交互流畅 |
| 📎 **对象存储** | 七牛云 OSS 集成，文件安全可靠 |

---

## 🖥️ 界面预览

> 以下为系统主要界面的功能概览。

### 登录与首页
- 统一登录入口，角色无差别登录
- 首页仪表盘展示统计概览与待办提醒

### 师生双选
- 志愿填报界面：学生按优先级填报 1-3 个意向导师
- 反选确认界面：导师查看报选学生列表并确认
- 轮次管理：管理员推进双选轮次、启动补选

### 学籍管理
- 学生信息列表：支持按学号、姓名、年级等条件筛选
- 学籍异动：休学 / 复学 / 退学 / 延期毕业申请与审批

### 课程管理
- 课程表视图：按学期、周次展示课程安排
- 成绩管理：成绩录入、加权计算、等级自动判定

### 学术管理
- 学术活动 / 成果 / 创新创业项目提交
- 三级审批流转（导师 → 教学秘书 → 分管院长）

### 论文管理
- 七环节流程可视化呈现
- 文件上传 / 下载 / 在线预览（PDF）

### 学位管理
- 前置条件自检看板
- 学位申请状态跟踪

---

## ⚠️ 注意事项

1. **数据库配置**：`application.properties` 部署前请务必修改为自有数据库地址。
2. **密钥安全**：七牛云 AccessKey/SecretKey 等敏感信息需自行配置。
3. **端口占用**：后端默认 8088，前端默认 89，如端口冲突请修改对应配置文件。
4. **前端代理**：开发环境下 `/dev-api` 代理到后端 8088，生产环境需配置 Nginx 反向代理。
5. **若依框架**：前端基于 RuoYi 3.8.9 开发，部分组件和工具函数来自框架，详细信息请参考 [若依官方文档](http://doc.ruoyi.vip/)。

---

## 🤝 贡献与反馈

本项目为本科毕业设计作品，欢迎提出改进建议和 Bug 反馈。如发现问题或有改进想法，欢迎提交 Issue 或 Pull Request。

---

## 📄 许可证

本项目仅用于**教育与学习目的**。



