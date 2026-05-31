# Base Frontend 组件地图

本文档登记 base-frontend 已沉淀的跨 app 通用组件和基础设施。新增任意前端组件前，先确认它是否属于这里、domain、aggregation 或 app 内部。

## UI Kit

| 能力 | 导入 | 适用场景 | 复用要求 |
|---|---|---|---|
| `FButton` | `@forest/ui-kit` | 通用按钮基础样式和交互 | 无业务语义按钮优先复用 |
| `FFieldError` | `@forest/ui-kit` | 表单字段错误提示 | 表单错误展示优先复用 |
| theme foundation / presets | `@forest/ui-kit` | 跨 app 主题基础变量和预设 | app 不重复定义公共主题基础 |

## 边界

- base-frontend 只放无业务语义、跨 app 复用的基础能力。
- user、organization、access 等业务语义组件放在对应 domain frontend。
- 多 domain 页面放在 aggregation frontend。
- app 专属导航、路由壳和一次性页面片段可以留在 app。

## Review 检查

- 新增基础组件前，确认它不依赖具体业务文案、权限、企业、用户或 app 路由。
- 如果组件需要业务类型或业务 API，优先放到 domain/aggregation。
- 如果只是某个 app 的局部视觉片段，先留在 app，不提前下沉到 base。
