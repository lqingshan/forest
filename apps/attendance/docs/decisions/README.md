# 决策记录

本目录用于记录 Attendance app 的关键取舍。

记录标准：

| 字段 | 说明 |
|---|---|
| 背景 | 当时遇到的问题 |
| 决策 | 选择了什么方案 |
| 原因 | 为什么这么选 |
| 影响 | 对代码、业务、后续演进的影响 |

当前已确定但暂不单独拆 ADR 的共识：

| 决策 | 说明 |
|---|---|
| 一期只做 PC Web | 不做移动端、小程序端 |
| app 只做装配 | 考勤业务后续进入 `business/domains/attendance` |
| 企业后台复用 workspace | 使用 `ADMIN token + X-Organization-No` |
| 平台端复用 platform 准入 | 使用 `PLATFORM token + 平台企业配置 + PLATFORM:{boundaryId}` |
