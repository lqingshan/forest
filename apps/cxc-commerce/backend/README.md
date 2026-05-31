# CXC Commerce Backend

后端目录用于 CXC Commerce 应用装配，当前已具备最小可运行认证能力。

当前职责：

- 装配 `business/domains/user` 通用身份认证能力。
- 暴露 CXC 项目级认证 API。
- 管理 CXC 项目级配置，如 `appCode`、`clientCode`、微信小程序配置、开放路径配置。
- 复制 user domain Flyway migration，形成独立 CXC 账号认证数据结构。

不建议在本目录沉淀通用账号中心、验证码、会话、商城核心领域模型。
