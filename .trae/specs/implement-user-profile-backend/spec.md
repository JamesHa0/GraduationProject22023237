# 个人中心后端能力 Spec

## Why
当前前端个人中心页面已调用 `/system/user/profile*` 系列接口，但后端未实现对应能力，导致页面无法正常获取与更新个人信息。  
需要补齐最小可用后端能力，并与现有用户模型保持一致，尽快打通“查看资料、修改资料、修改密码、上传头像”流程。

## What Changes
- 新增个人中心查询接口：`GET /system/user/profile`
- 新增个人中心资料更新接口：`PUT /system/user/profile`
- 新增个人中心密码更新接口：`PUT /system/user/profile/updatePwd`
- 新增个人中心头像上传接口：`POST /system/user/profile/avatar`
- 调整前端个人中心字段绑定，改为使用当前后端 `User` 实体字段（`name/phone/email/gender`）
- 头像上传第一阶段返回占位 `imgUrl`，不做真实文件落盘
- **BREAKING**：前端个人中心不再依赖 RuoYi 风格字段（如 `nickName/phonenumber/dept/roleGroup/postGroup`）

## Impact
- Affected specs: 用户个人资料管理、用户凭证管理、头像展示链路
- Affected code: `SystemUserController`、用户相关 DTO、个人中心前端页面与 API 封装

## ADDED Requirements
### Requirement: 个人资料查询
系统 SHALL 提供当前登录用户个人资料查询能力，并返回可直接用于个人中心展示的字段。

#### Scenario: 查询成功
- **WHEN** 已登录用户访问 `GET /system/user/profile`
- **THEN** 返回该用户的 `id/username/name/phone/email/gender/createTime` 等字段（不含密码）

#### Scenario: 未登录或会话失效
- **WHEN** 请求未携带有效 Token 或缓存中无会话
- **THEN** 返回失败结果，并由前端按现有机制处理重登录

### Requirement: 个人资料更新
系统 SHALL 允许当前登录用户更新其基础资料（姓名、手机号、邮箱、性别），并持久化更新时间。

#### Scenario: 更新成功
- **WHEN** 用户提交合法资料到 `PUT /system/user/profile`
- **THEN** 更新当前用户资料并返回成功结果

### Requirement: 个人密码更新
系统 SHALL 支持当前登录用户修改密码，且必须校验旧密码正确性。

#### Scenario: 修改成功
- **WHEN** 用户提交正确旧密码与合法新密码到 `PUT /system/user/profile/updatePwd`
- **THEN** 更新密码并返回成功结果

#### Scenario: 旧密码错误
- **WHEN** 用户提交的 `oldPassword` 与当前密码不一致
- **THEN** 返回失败结果并提示旧密码错误

### Requirement: 头像上传（阶段一）
系统 SHALL 接收头像上传请求并返回可展示的 `imgUrl`，用于打通个人中心头像更新流程。

#### Scenario: 上传流程打通
- **WHEN** 用户调用 `POST /system/user/profile/avatar`
- **THEN** 返回成功结果与占位 `imgUrl`

## MODIFIED Requirements
### Requirement: 个人中心前后端字段契约
个人中心页面字段契约从 RuoYi 风格命名调整为当前项目用户模型命名。  
页面渲染和提交逻辑应使用 `name/phone/email/gender`，并兼容后端统一返回结构。

## REMOVED Requirements
### Requirement: 个人中心依赖部门/岗位/角色分组展示
**Reason**: 当前后端数据模型未提供 `dept/postGroup/roleGroup` 的稳定契约，强行兼容会增加无效复杂度。  
**Migration**: 个人中心先展示用户基础资料；若后续需要组织信息，另开独立需求并补齐组织模型接口。
