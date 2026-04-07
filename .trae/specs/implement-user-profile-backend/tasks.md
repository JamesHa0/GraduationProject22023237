# Tasks
- [x] Task 1: 完成个人中心后端接口骨架与契约
  - [x] SubTask 1.1: 在 `SystemUserController` 增加 `/profile`、`/profile/updatePwd`、`/profile/avatar` 路由方法
  - [x] SubTask 1.2: 定义请求/响应 DTO（资料更新、密码更新、头像返回）并明确字段映射
  - [x] SubTask 1.3: 使用 `CurrentUserUtil` 获取当前用户上下文并处理未登录场景

- [x] Task 2: 实现个人资料查询与更新能力
  - [x] SubTask 2.1: 实现 `GET /system/user/profile` 返回当前用户可展示信息（不含密码）
  - [x] SubTask 2.2: 实现 `PUT /system/user/profile` 更新 `name/phone/email/gender` 与 `updateTime`
  - [x] SubTask 2.3: 增加必要参数校验与失败信息返回

- [x] Task 3: 实现个人密码修改能力
  - [x] SubTask 3.1: 实现 `PUT /system/user/profile/updatePwd` 入参与规则校验
  - [x] SubTask 3.2: 校验旧密码正确性后更新新密码与更新时间
  - [x] SubTask 3.3: 覆盖失败分支（旧密码错误、新密码非法、用户不存在）

- [x] Task 4: 打通头像上传占位流程（阶段一）
  - [x] SubTask 4.1: 实现 `POST /system/user/profile/avatar` 接口并返回占位 `imgUrl`
  - [x] SubTask 4.2: 与前端上传调用保持参数兼容，保证页面可展示新头像地址

- [x] Task 5: 同步个人中心前端字段契约
  - [x] SubTask 5.1: 更新 `src/views/system/user/profile` 页面字段绑定为 `name/phone/email/gender`
  - [x] SubTask 5.2: 移除对 `dept/roleGroup/postGroup/nickName/phonenumber` 的强依赖
  - [x] SubTask 5.3: 保持个人中心“查看资料 + 修改资料 + 修改密码 + 修改头像”全链路可用

- [x] Task 6: 验证与回归
  - [x] SubTask 6.1: 通过本地编译或最小接口联调验证新增接口可用
  - [x] SubTask 6.2: 校验前端个人中心页面无阻断错误（已通过：`pnpm build:prod`）
  - [x] SubTask 6.3: 清理明显日志噪音并确认不破坏现有用户管理接口（已完成：清理 `src/utils/request.js` 调试 `console.log`）

- [x] Task 7: 修复验证失败项并完成闭环
  - [x] SubTask 7.1: 修复 `src/views/system/field/index.vue` 第 210 行模板语法错误，恢复前端可构建
  - [x] SubTask 7.2: 清理 `src/utils/request.js` 中明显调试日志，避免噪音输出
  - [x] SubTask 7.3: 完成最小联调（查询资料、修改资料、修改密码、修改头像）并补齐命令与结果记录

## 验证记录
- 前端构建（通过）：
  - 命令：`pnpm build:prod`
  - 结果：构建成功，`exit code 0`
- 最小联调（通过，端口 `8089`）：
  - 后端启动命令：`.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=8089" -DskipTests`
  - 联调命令：PowerShell 调用 `/login`、`GET /system/user/profile`、`PUT /system/user/profile`、`PUT /system/user/profile/updatePwd`、`POST /system/user/profile/avatar`
  - 结果摘要：`{"login":"success","profile":"success","updateProfile":"success","updatePwd":"success","avatar":"success"}`
  - 回滚处理：密码回滚为 `admin123`、资料恢复为可用值均成功

# Task Dependencies
- Task 2 依赖 Task 1
- Task 3 依赖 Task 1
- Task 4 依赖 Task 1
- Task 5 依赖 Task 2、Task 3、Task 4
- Task 6 依赖 Task 2、Task 3、Task 4、Task 5
- Task 7 依赖 Task 6
