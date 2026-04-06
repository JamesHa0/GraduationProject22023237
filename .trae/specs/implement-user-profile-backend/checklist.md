* [x] `GET /system/user/profile` 可返回当前登录用户资料且不包含密码字段

* [x] `PUT /system/user/profile` 可成功更新 `name/phone/email/gender` 并持久化 `updateTime`

* [x] `PUT /system/user/profile/updatePwd` 能正确校验旧密码并更新新密码

* [x] 旧密码错误时返回失败结果，前端可展示明确提示

* [x] `POST /system/user/profile/avatar` 返回可用的占位 `imgUrl`

* [x] 个人中心页面字段已切换为 `name/phone/email/gender`，无 `nickName/phonenumber` 阻断

* [x] 个人中心页面不再依赖 `dept/roleGroup/postGroup` 且可正常渲染

* [x] 新增改造不破坏现有 `/system/user` 列表、增删改查接口

* [x] 本地最小联调通过（个人资料查询、修改资料、修改密码、修改头像）

