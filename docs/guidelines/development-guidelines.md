# Forest 开发规范

## 1. 目的

这份文档作为项目的统一开发规范入口，覆盖：

- 模块边界与依赖规则
- 跨域查询与聚合实现规则
- 数据库设计与迁移规则
- JPA 查询方式选择规则
- Redis KEY 命名与缓存使用规则
- Maven parent、reactor 与应用独立构建规则
- 后台前端交互规则
- 测试与启动规则

如果某条规范会影响多个模块的实现方式，应优先更新本文件；架构级说明仍保留在 [architecture.md](../architecture/architecture.md)。

专项规范：

- [database-guidelines.md](./database-guidelines.md)：数据库设计、迁移和 SQL 开发规范。
- [jpa-query-guidelines.md](./jpa-query-guidelines.md)：JPA 查询方式选择规范。
- [redis-key-guidelines.md](./redis-key-guidelines.md)：Redis KEY 命名、DB index 隔离、TTL 和后续 Redis 接入规范。
- [frontend-web-guidelines.md](./frontend-web-guidelines.md)：PC Web app、企业工作台页面、权限展示和前端状态管理规范。
- [frontend-component-reuse.md](./frontend-component-reuse.md)：前端组件复用、组件地图和 review 检查项。
- [app-guidelines.md](./app-guidelines.md)：`apps/*` 建壳、文档结构和新 app 前置检查规范。

模块专项文档：

- [business/domains/file/docs/file-module-design.md](../../business/domains/file/docs/file-module-design.md)：文件上传、私有下载、OSS、元数据和多文件上传交互规范。
- [business/domains/organization/docs/workspace-guidelines.md](../../business/domains/organization/docs/workspace-guidelines.md)：organization、企业工作台上下文和企业认证 Gate 规范。
- [business/domains/access/docs/rbac-guidelines.md](../../business/domains/access/docs/rbac-guidelines.md)：Access、RBAC、权限点、角色和平台权限边界规范。
- [business/domains/user/docs/auth-architecture.md](../../business/domains/user/docs/auth-architecture.md)：user/account/auth/session/token 认证边界。
- [apps/cxc-commerce/docs/mobile-auth-design.md](../../apps/cxc-commerce/docs/mobile-auth-design.md)：CXC Android/iOS + H5 移动端登录方案。
- [base-backend/docs/carrier-auth.md](../../base-backend/docs/carrier-auth.md)：APP 本机号一键登录的后端技术抽象。

## 2. 顶层原则

- 先守住模块边界，再追求局部实现“省事”。
- 默认按高内聚、低耦合设计模块：一个模块只承担清晰、稳定的职责，跨模块协作通过明确接口完成。
- 面向对象设计优先遵守单一职责、开闭原则、依赖倒置、接口隔离和组合优先，避免把供应商 SDK、缓存实现或具体 app 逻辑写进核心业务对象。
- 设计模式用于隔离变化点，不为模式而模式；认证、支付、短信、第三方平台等多实现能力优先考虑策略、适配器、工厂、门面和模板方法等模式。
- 业务之间必须解耦：通用账号、认证、用户、组织、订单、支付等能力只通过接口或事件协作，不互相侵入实体和表。
- 业务和技术必须解耦：业务表达“校验验证码”“解析本机手机号”“创建会话”等意图，Redis、JWT、阿里云、微信、短信厂商等技术细节放在 adapter/provider 层。
- 外部 HTTP API 优先稳定，内部 service 能力可以按需求渐进扩展。
- 单域能力放在 `business/domains/*`，跨域编排放在 `business/aggregations/*`。
- 优先让查询语义正确、分页稳定、测试可验证，再考虑缓存或读模型优化。
- Redis 使用单容器、多 DB index 隔离环境；KEY 不包含 `local` / `prod`，业务应用通过 `forest:{app}:...` prefix 区分，具体规则见 [redis-key-guidelines.md](./redis-key-guidelines.md)。
- 后端 Redis 访问必须通过 `forest-starter-redis`，业务代码禁止手拼 Redis KEY，禁止直接注入 `StringRedisTemplate` / `RedisTemplate`。
- 文件上传、下载和对象存储访问必须通过 `business/domains/file` 与 `forest-starter-object-storage`，业务模块只保存 `fileNo`，不直接保存 OSS 永久地址。
- 后台页面默认服务运营场景，展示格式要统一、可读、可复现。
- 前端开发默认先查组件地图、优先复用已有 base/domain/aggregation/app 组件，再新增组件。

### 2.1 短信验证码配置原则

短信验证码内容由配置决定。发送验证码时会写入 Redis 和短信发送日志；登录校验时必须从 Redis 读取并消费验证码，不提供绕过校验的测试码。

```yaml
forest:
  verification:
    sms:
      code: ${AUTH_SMS_CODE:121314}
```

规则：

- `forest.verification.sms.code` 默认值为 `121314`，可通过 `AUTH_SMS_CODE` 覆盖。
- `FOREST_SMS_PROVIDER=mock` 只跳过真实短信供应商调用，不跳过验证码生成、Redis 缓存、短信日志和登录校验。
- 用户必须先点击“发送验证码”，让验证码写入 Redis；登录时输入同一个验证码，后端按完整流程校验并消费。
- `FOREST_SMS_LOG_CONTENT_MODE=PLAIN` 只建议 local 使用，方便从 `sms_send_log.content_snapshot` 查看验证码。
- `FOREST_SMS_PROVIDER=aliyun` 会真实调用阿里云短信；生产环境应在短信签名和模板审核通过后启用。
- `FOREST_SMS_PROVIDER=disabled` 表示短信发送不可用，发送验证码会返回明确错误。
- 登录日志统一记录 `verification_mode = SMS_CODE`。

### 2.1.1 短信与验证码职责

- `forest-starter-sms` 是短信供应商技术入口，一期支持 `aliyun`、`mock`、`disabled`。
- `business/domains/notification` 负责记录所有短信发送行为，统一落库到 `sms_send_log`。
- `business/domains/verification` 负责验证码状态、发送冷却、每日上限、错误次数和 ticket，一律使用 Redis。
- `business/domains/user` 只表达手机号登录等认证业务，不再持有 `sms_code` 表和验证码存储逻辑。
- `sms_code` 已废弃并通过 Flyway 删除，后续不得新增或继续依赖。

### 2.2 外部边界归一化原则

前后端统一采用“在对外部封装时处理好输入”的原则。

核心规则：

- 外部输入只允许在边界层保持不确定类型；进入内部业务层前必须完成校验、转换、兜底和语义命名。
- 内部 service、domain API、frontend API、view-model 和 app 编排层应流转明确类型，不把 `unknown`、`any`、原始 HTTP body、原始微信事件、原始第三方响应继续向内传递。
- 后端 Controller、消息消费入口、第三方回调入口、开放 API adapter 是后端外部边界；它们负责把请求 DTO 转成内部 command/query，并完成基础格式校验。
- 前端微信事件、浏览器事件、URL query、storage、HTTP response、第三方 SDK response 是前端外部边界；它们负责把原始事件或响应转成内部明确类型。
- 内部方法不应通过“字段是否为空”改变接口语义；不同业务语义应使用不同接口、不同 command 或不同工厂方法，内部公共逻辑再复用。

示例：

```text
微信原生事件 event.detail.code
= 外部输入边界，可以按 unknown 处理

组件适配层
= 校验 event.detail.code 是否为 string，并归一化为 phoneCode: string

内部 auth API context
= 使用 phoneCode?: string，不再使用 unknown
```

后端示例：

```text
Controller RequestBody
= 外部输入边界，负责 DTO 校验和语义拆分

Service command/query
= 内部明确类型，不继续携带原始 RequestBody 或含糊字段
```

判断标准：

- 如果数据来自用户、HTTP、微信/支付宝、浏览器、storage、MQ、定时任务参数或第三方 SDK，它就是外部输入。
- 如果一个对象已经命名为 `Context`、`Command`、`Query`、`Request` 之外的内部业务对象，默认应是干净类型。
- 如果内部代码还需要反复写 `typeof xxx === ...`、空字符串兜底、第三方错误码解析，说明边界处理下沉得太晚，需要前移。

### 2.3 事件解耦与事务语义

跨域初始化或跨域副作用优先通过事件解耦，避免 domain 直接依赖另一个 domain 的实现类。

当前约定：

- 跨域事件类型放在 `business/common`，避免具体 domain 双向依赖。
- 如果业务要求强一致，使用同步 `@EventListener`，让监听器加入当前事务。
- 如果事件处理失败必须让主流程失败，则不要使用 `@TransactionalEventListener(AFTER_COMMIT)`。
- 如果可以接受主事务先提交、后续异步补偿，再单独设计 `AFTER_COMMIT`、重试和补偿状态表。

示例：

```text
OrganizationEntryApplicationService.createOrganization()
-> 创建 organization / 默认部门 / owner member
-> publish OrganizationCreatedEvent
-> access listener 初始化默认角色和 owner 授权
```

企业创建后的 RBAC 初始化要求强一致，因此使用同步 `@EventListener`：RBAC 初始化失败时，企业创建整体回滚。

### 2.4 代码质量约定

- 复杂上下文对象优先提供工厂方法，例如 `AccessCheckContext.organizationMember(...)`。
- 跨模块事件优先提供工厂方法，例如 `OrganizationCreatedEvent.of(...)`。
- 业务异常优先使用统一工厂，例如 `BusinessException.of(...)`。
- 枚举优先于散落字符串，例如工作台模式、业务状态、角色 code、权限 code。
- 注释必须说明真实职责和当前限制，不把未来计划写成已实现能力。
- 注释优先放在类、方法和复杂私有逻辑上，避免无意义逐行翻译。
- 只要方法职责涉及跨域编排，应在注释中说明“为什么放在 aggregation”。

## 3. 模块边界规范

### 3.0 Maven parent 与 reactor 规范

后端 Maven 文件职责必须区分清楚：

- `base-backend/pom.xml` 是公共 parent，负责统一版本、依赖管理、插件管理；现阶段也保留本地全量构建入口职责。
- `apps/*/backend/pom.xml` 是应用模块 POM，只描述应用自身依赖、资源复制和打包方式。
- `base-backend/pom.<app>.xml` 是应用专用 reactor，负责 Docker / CI 场景下从源码构建该应用所需模块。

当前应用专用 reactor：

```text
base-backend/pom.trade-leads.xml
base-backend/pom.cxc-commerce.xml
```

构建约束：

- `trade-leads` Docker 构建必须使用 `pom.trade-leads.xml`，不允许拷贝或扫描 `apps/cxc-commerce/backend`。
- `cxc-commerce` 后续 Docker 构建必须使用 `pom.cxc-commerce.xml`，不允许拷贝或扫描 `apps/trade-leads/backend`。
- 应用专用 reactor 只放该应用实际需要的 `starter`、`business/domain`、`business/aggregation` 和当前 app backend 模块。
- 新增业务应用如果进入 Docker / CI 独立构建，需要同步新增对应 `base-backend/pom.<app>.xml`，不要把其他 app 作为 Docker 构建上下文的一部分。

### 3.1 `business/domains/*`

单业务域模块负责本域的核心能力：

- 实体与表
- 核心 service
- 本域 controller
- 本域前端 API、类型、展示模型，以及不跨域的展示组件
- 原生小程序组件可以放在 domain frontend，由 app 小程序构建脚本以 `dist/modules/*` 方式装配

例如：

- `user` 负责用户筛选、资料读取、状态管理
- `point` 负责积分余额、流水、加减分、账本查询

### 3.2 `business/aggregations/*`

聚合模块负责跨域组合能力：

- 聚合查询
- 聚合业务编排
- 跨域页面工作台

约束：

- aggregation 可以依赖多个 domain
- domain 不能反向依赖 aggregation
- aggregation 不拥有下游 domain 的实体和表
- aggregation 不直接跨域访问 repository，应通过下游 service 接口访问

### 3.3 `apps/*`

`apps/*` 只负责应用装配与 app 专属编排：

- 组合多个 `business` 模块形成完整系统
- 放 app 独有流程
- 小程序 app 只配置页面列表、路由策略、storage prefix、登录 redirect、跨域跳转和业务模块组合

不放：

- 通用单域能力
- 多个 app 都会复用的聚合查询
- 多个微信小程序都会复用的 `wx.request`、storage、router、payment、session store 等平台基础设施

### 3.4 `base-frontend`

`base-frontend` 承载跨 app、跨业务的前端基础设施：

- `@forest/http-client` 负责通用 HTTP client 和可注入 transport。
- `@forest/wechat-miniapp-platform` 负责微信小程序平台适配，不依赖任何 business 包。
- `@forest/wechat-miniapp-client-session` 负责通用 client session 编排，通过注入 `loginByWechat`、`fetchCurrentUser` 等业务函数使用。
- `@forest/wechat-miniapp-client-app` 负责把 platform、session 和 app definition 组装成具体小程序可直接使用的 facade。

具体小程序在 app 层创建 `app-definition.ts` 和 `miniapp-app.ts`：

- `app-definition.ts` 注入后端地址、登录页、默认页、一级页和 storage prefix
- `miniapp-app.ts` 暴露 auth/router/payment/lifecycle facade

页面不直接维护通用 platform 实现，也不直接 import `@forest/wechat-miniapp-platform` 或 `@forest/wechat-miniapp-client-session`。

### 3.4.1 微信小程序页面规则

微信小程序 Page 的目标不是“尽可能什么都不写”，而是只保留属于 app 装配层的职责。

Page 应保留：

- 生命周期：`onLoad`、`onShow`、`onPullDownRefresh`、`onReachBottom`
- 登录守卫
- 路由跳转
- 微信平台动作：`wx.showModal`、`wx.showToast`、`wx.showLoading`、`wx.hideLoading`、`wx.setClipboardData`
- 微信小程序支付调起时机
- 跨 domain 编排
- page 自己的 `setData(...)`

Page 不应继续保留：

- 业务空态文案
- 业务错误 fallback 文案
- 业务状态映射
- 单一业务分页 merge
- 单一业务卡片和列表结构

这些内容应下沉到 `business/*/frontend/src/wechat-miniapp` 下的：

- `view-model.ts`
- `state.ts`
- `pager.ts`
- `loader.ts`
- 原生业务组件目录

判断一个页面是否“成熟”，看的是职责是否放对层，而不是简单比较文件长短：

- 如果页面只剩守卫、刷新、触底、路由、平台动作，它就已经比较稳定
- 如果页面仍然较厚，但厚度来自跨 domain 编排或微信平台动作，这仍然是合理的 app 组合页
- 只有当页面还持有单一业务的文案、状态机、卡片结构时，才说明还应继续下沉

### 3.4.2 微信小程序 app 固定资产

下面这些资产默认留在 app 层，不下沉到 business：

- app 级导航，例如 `bottom-nav`
- 页面壳布局，例如 `shell-page`、`shell-stack`、`shell-back`
- 一级页面打开策略
- 登录 redirect 策略
- 微信平台动作的调用时机
- 微信小程序支付的调起与结果页跳转

理由很简单：这些能力描述的是“这个客户端怎么运行”，不是“这个业务怎么展示”。

### 3.4.3 Domain Frontend 样式边界

`business/*/frontend` 可以提供本域 API、类型、view-model、状态机和不跨域的组件结构，但不应写死某个 app 的品牌视觉。

通用 domain 组件允许：

- 提供必要结构 class。
- 提供 `externalClasses`、CSS 变量或等价样式 hook。
- 保留最低限度布局，保证组件结构可用。

通用 domain 组件不允许：

- 写死 app 品牌色。
- 写死 app 卡片阴影、页面背景、营销风格。
- 把某个 app 的按钮、间距、圆角、主题复制进通用模块。

app 负责决定最终视觉样式。以文件上传组件为例，`@forest/file` 只提供多文件选择、上传状态、进度、局部失败、局部重试和 class hook；`cxc-commerce`、`trade-leads` 等 app 自己决定按钮、卡片、成功/失败状态和页面布局。

### 3.5 依赖方向

- `business` 不能依赖 `apps`
- `business/domains/*` 不能依赖 `business/aggregations/*`
- `business/aggregations/*` 可以依赖 `business/domains/*`
- `app` 可以组合多个 `business`
- 微信小程序公共 platform/session 包不能依赖 `business`
- `business/*/frontend` 不能依赖 `apps/*` 或微信小程序 app/platform 装配
- `controller -> service interface -> impl -> repository`

### 3.6 命名规范

- 对象、类、接口、类型、文件名默认使用单数命名。
- HTTP API 路径默认使用单数命名，例如 `/api/platform/user`、`/api/platform/lead`、`/api/platform/user-point`。
- HTTP API 顶层前缀必须遵守三端分层：`/api/auth/**` 表示认证入口，`/api/client/**` 表示普通用户端，`/api/admin/**` 表示商家后台 / 企业后台，`/api/platform/**` 表示平台后台，`/api/open/**` 表示外部回调或公开入口。
- 后端 controller 必须引用 `ForestApiPaths` 顶层常量，不直接手写 `/api/auth`、`/api/client`、`/api/admin`、`/api/platform`、`/api/open`。
- 前端 API 调用应优先引用 `@forest/http-client` 的 `apiPaths`，并按端使用 `clientHttp`、`adminHttp`、`platformHttp`。
- `admin` 不表示平台后台；平台后台必须使用 `platform`。商家后台、企业后台统一使用 `admin`。
- 数据表默认使用 `snake_case` 单数命名，例如 `lead`、`point_log`、`user_account`、`recharge_order`。
- 如果单数名直接命中数据库关键字或存在明显落地风险，必须使用“可执行的单数替代名”，例如 `app_user`，不要硬上保留字 `user`。
- 关联表使用单数桥接名，例如 `user_account`，不要使用复数桥接名。
- 命名重构必须同步修改实体、Flyway migration、约束名、SQL、测试、文档，不能只改一层。

## 4. 跨域查询与聚合规范

### 4.1 什么时候是跨域查询

如果一个查询同时依赖多个 domain 的过滤、排序、补充信息或明细，就属于跨域查询，必须放在 aggregation。

典型例子：

- `user-lead` 先使用 `lead` 域的基础搜索能力，再组合当前用户解锁状态、联系方式遮罩和扣积分动作；因此小程序用户线索接口放在 aggregation，不放在 `domain/lead/client`
- `user-point` 既依赖 `user` 的筛选能力，又依赖 `point` 的积分排序与日志能力

### 4.2 先判断“过滤字段属于谁，排序字段属于谁”

设计聚合查询前，必须先明确：

- 过滤字段在哪个 domain
- 排序字段在哪个 domain
- 分页应该由哪个 domain 主导

如果排序字段在 `point`，就不能先在 `user` 域分页，否则分页结果会失真。

### 4.3 运行时临时拼接的默认链路

当项目优先级是“模块解耦、数据库解耦、查询条件可动态增加”时，优先采用运行时临时拼接。

标准链路：

1. `user` 域按条件查全部匹配 `userIds`
2. `point` 域在这批 `userIds` 内负责排序分页
3. `user` 域按当前页 `userIds` 批量补用户摘要
4. aggregation 负责组装最终结果

约束：

- 当前阶段不引入 `searchToken`
- 当前阶段不引入缓存协议
- 当前阶段不引入聚合索引表
- 未来若运行时复杂度不可接受，再单独评估读模型方案

### 4.4 运行时临时拼接的实现要求

- 外部 HTTP API 路径尽量保持不变
- 内部 service 能力可以新增，但应保持“各域只解释自己负责的条件”
- 列表排序必须固定、稳定，并带次级排序字段
- 列表分页和详情日志分页必须独立
- 空条件是否允许，必须在需求里明确，并保证前后端一致

### 4.5 当前后台查询页的约定

后台工作台类页面默认采用以下模式：

- 左侧：搜索条件 + 列表 + 列表分页
- 右侧：当前选中对象详情 + 明细日志 + 明细分页

对于这类主从页面：

- 选中对象通过路由 query 持久化，例如 `?userId=...`
- 刷新列表后，如果当前选中对象不在当前页，应清空选中态
- 切换对象时加载详情和日志第一页
- 日志翻页时只刷新日志，不重复拉详情

## 5. JPA 查询规范

这部分整合了原 `docs/guidelines/jpa-query-guidelines.md` 的内容。

### 5.1 默认选择顺序

新增查询时，默认按下面顺序判断：

1. 先考虑 `findBy...`
2. 不够表达时再考虑 `@Query`
3. 只有“多可选条件动态组合”时才考虑 `Specification`

目标不是“技术统一”，而是：

- 业务意图清楚
- 代码量小
- 维护成本低
- 索引与事务行为可预期

### 5.2 用 `findBy...` 的场景

满足下面大部分条件时，优先使用派生查询方法：

- 查询条件固定，不是动态拼装
- 条件数量少，通常 1 到 3 个
- 查询语义清晰，方法名不会过长
- 主要是等值、存在性、简单排序、单表分页
- 不需要更新语句、聚合语句、复杂关联或数据库特性

推荐：

```java
Optional<AccountPO> findByTypeAndIdentifier(String type, String identifier);

boolean existsByUserIdAndLeadId(Long userId, Long leadId);

Page<PointLogPO> findByUserIdOrderByCreatedTimeDesc(Long userId, Pageable pageable);
```

补充约束：

- 能用 `existsBy...` 就不要查整行再判空
- 能用 `findBy...And...` 清楚表达的，不要提前升级到 `@Query`

### 5.3 用 `@Query` 的场景

当派生查询表达不了，或者表达出来会失真、冗长、难维护时，使用 `@Query`。

典型场景：

- `update` / `delete` / 批量修改
- 聚合、投影或固定复杂语义
- 需要数据库原子更新保证并发正确性

推荐约束：

- `@Modifying` 只用于真正的写语句
- 返回受影响行数，用于判断更新是否成功
- 优先 JPQL；只有 JPQL 不够时才使用原生 SQL
- 使用原生 SQL 时，注释说明原因

不推荐：

- 简单等值查询也用 `@Query`
- 仅因为“风格统一”就把简单派生查询改成 `@Query`

### 5.4 用 `Specification` 的场景

只有在“查询条件是动态可选组合”时才使用 `Specification`。

适用场景：

- 后台分页筛选
- 3 个及以上可选筛选条件
- 条件组合开放，不适合声明成大量 `findByAAndBAndC...`

当前项目约束：

- `Specification` 只用于读查询，不用于写操作
- 主要用于后台分页筛选接口
- 由 service 根据 `PageQuery` 组装
- 字段引用优先使用 JPA Static Metamodel，避免魔法字符串

推荐：

```java
Specification<UserPO> specification = Specification
    .where(UserSpecifications.withId(safeQuery.id()))
    .and(UserSpecifications.nameContainsIgnoreCase(safeQuery.name()))
    .and(UserSpecifications.withStatus(safeQuery.status()));
return userRepository.findAll(specification, pageable);
```

### 5.5 Repository 与 service 的职责

- controller 负责接收参数
- service 负责解释查询语义、校验输入、决定查询策略
- repository 只暴露当前业务真正需要的方法

不推荐：

- Repository 预铺所有字段的查询方法
- 生成“大而全”的全字段 `Specs` 工具箱

### 5.6 并发敏感写操作

涉及余额、库存、计数器等写操作时，优先保证并发正确性。

推荐方式：

- 单条条件更新
- 返回影响行数
- 搭配版本号、余额判断等条件

不要为了“统一查询风格”硬套 `findBy...` 或 `Specification`。

## 6. 后台前端交互规范

### 6.1 搜索与分页

- 空条件是否允许，必须在需求里明确
- 校验只对非空字段生效
- 默认分页大小优先统一为 `20`
- 后端固定排序优先于前端自由排序
- 只要结果涉及分页，排序必须稳定且可重复

### 6.2 展示格式

- 后台时间默认展示到分钟
- 统一格式为 `YYYY-MM-DD HH:mm`
- 纯展示格式变化优先在前端格式化层处理，不改后端返回结构

### 6.3 状态同步

- 路由负责保存当前选中对象
- 新查询结果不包含当前选中对象时，应清空选中态
- 列表区域与详情区域的请求边界要清晰，避免无谓重复请求

## 7. 测试规范

每次需求实现至少覆盖对应层级的最小验证闭环。

### 7.1 domain 层

新增内部 service 能力时，需要补 domain 层测试，至少覆盖：

- 正常路径
- 输入校验
- 排序或分页语义

### 7.2 aggregation 层

聚合查询至少覆盖：

- 空条件行为
- 输入校验行为
- 排序正确性
- 分页正确性

### 7.3 前端

后台页面至少覆盖：

- 空条件查询
- 主从联动
- 独立分页
- 关键展示格式，例如时间精度

### 7.4 真实运行验证

完成页面改动后，除单测外还应至少做一次真实启动验证，确认：

- 服务能启动
- 页面能打开
- API 能响应
- 当前修改已经进入实际运行镜像或 dev 服务

## 8. 启动与环境规范

启动项目之前，先读仓库文档，不凭记忆猜环境。

当前默认环境：

- 后端：`JDK 25 + Spring Boot 4.0.5 + PostgreSQL 18.3`
- 前端：`Node 20 + pnpm 10.33 + Vue 3.5 / Vite 6`

推荐启动方式：

```bash
./deploy/scripts/trade-leads.sh local up
```

当前本地启动默认不再拉起 PostgreSQL Docker。后端连接哪个 PostgreSQL 由当前 app 的 `apps/<app>/env/*.env` 和 `SPRING_DATASOURCE_URL` 决定；不同 app 使用独立数据库，例如 `trade_leads_local`、`cxc_commerce_local`、`attendance_local`。远程数据库端口只应对可信开发机 IP 放行。

本地开发模式：

```bash
cd apps/trade-leads/backend
mvn spring-boot:run

cd base-frontend
pnpm install
pnpm dev --filter @forest/trade-leads-platform-web
```

交付时建议同时说明：

- 启动命令
- 访问地址
- 默认账号
- 是否验证成功

## 9. 数据库规范

数据库规范已拆出为独立文档：

- [database-guidelines.md](./database-guidelines.md)

本文件只保留入口链接，不再维护重复内容。数据库相关变更应优先阅读独立文档，尤其是模块归属、Flyway migration、金额单位、软删除、唯一约束、索引、外键和并发写入规范。

## 10. 文档维护规范

- 通用开发规范统一维护在本文件
- 架构边界与目录说明维护在 `docs/architecture/architecture.md`
- docs 目录入口维护在 `docs/README.md`
- 如果某个专题明显膨胀，再从本文件中拆出子文档
- 拆出后，本文件保留入口链接，不再维护重复内容
- 阶段性开发沉淀可以先放入专题文档，稳定后再合并进本文件
